package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.ide.inspections.LeftRecursiveProductionInspection
import com.intellij.util.ThreeState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

/**
 * Extensions to compute properties of BNF productions such as nullability,
 * start and follow set, left-recursivity, etc.
 *
 * @author Clément Fournier
 * @since 1.0
 */


/**
 * Returns true if this regexp element can match the empty string.
 */
fun JccRegexElement.isEmptyMatchPossible(alreadySeen: ImmutableList<JccRegexElement> = immutableListOf()): Boolean =
        when (this) {
            is JccLiteralRegexUnit        -> text.length == 2 // ""
            is JccCharacterListRegexUnit  -> false // assume that
            is JccParenthesizedRegexUnit  -> occurrenceIndicator.let {
                // test it whether there is a + or nothing
                it is JccZeroOrOne             // ?
                        || it is JccZeroOrMore // *
                        || it is JccRepetitionRange && it.first == 0 // if no first then this is false
                        || regexElement.isEmptyMatchPossible(alreadySeen) // +
            }
            is JccRegexSequenceElt        -> regexUnitList.all { it.isEmptyMatchPossible(alreadySeen) }
            is JccRegexAlternativeElt     -> regexElementList.any { it.isEmptyMatchPossible(alreadySeen) }
            is JccTokenReferenceRegexUnit ->
                if (this in alreadySeen) false // cyclic reference
                else typedReference.resolveToken()
                    ?.psiElement
                    ?.regularExpression
                    ?.getRootRegexElement(followReferences = false)
                    ?.isEmptyMatchPossible(alreadySeen.add(this)) == true
            else                          -> throw IllegalStateException(this.toString())
        }

/**
 * Returns true if this expansion can expand to the empty string. To determine nullability
 * of a production, use [JccNonTerminalProduction.isNullable] instead, which caches the result.
 * [LeftRecursiveProductionInspection] refreshed the cache regularly for the whole file.
 */
fun JccExpansion.isEmptyMatchPossible(alreadySeen: ImmutableList<JccNonTerminalProduction> = immutableListOf()): Boolean =
        when (this) {
            is JccParserActionsUnit          -> true
            is JccLocalLookaheadUnit         -> true
            is JccOptionalExpansionUnit      -> true
            is JccRegexExpansionUnit         -> false // todo use the above
            is JccScopedExpansionUnit        -> expansionUnit.isEmptyMatchPossible(alreadySeen)
            is JccAssignedExpansionUnit      -> assignableExpansionUnit?.isEmptyMatchPossible(alreadySeen) == true
            is JccParenthesizedExpansionUnit -> occurrenceIndicator.let {
                it is JccZeroOrOne || it is JccZeroOrMore
                        || expansion?.isEmptyMatchPossible(alreadySeen) == true // test it whether there is a + or nothing
            }
            is JccExpansionSequence          -> expansionUnitList.all { it.isEmptyMatchPossible(alreadySeen) }
            is JccExpansionAlternative       -> expansionList.any { it.isEmptyMatchPossible(alreadySeen) }
            is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction()?.let {
                it is JccBnfProduction && it.computeNullability(alreadySeen)
            } ?: false
            is JccTryCatchExpansionUnit      -> expansion?.isEmptyMatchPossible(alreadySeen) == true
            else                             -> false
        }

private fun JccBnfProduction.computeNullability(alreadySeen: ImmutableList<JccNonTerminalProduction> = immutableListOf()): Boolean {
    if (isNullable == ThreeState.UNSURE) {
        if (this in alreadySeen) {
            // left recursion detected!!
            return isNullable == ThreeState.YES
        }
        val computed = expansion?.isEmptyMatchPossible(alreadySeen.add(this))
        if (computed != null) {
            isNullable = if (computed) ThreeState.YES else ThreeState.NO
        } // else stays unsure
    }
    return isNullable == ThreeState.YES
}

/**
 * Gets the set of productions that this production can expand to without consuming any tokens.
 * This is used to check for left-recursion. If null then either a production reference couldn't
 * be resolved or this is a javacode production.
 */
fun JccNonTerminalProduction.leftMostSet(): Set<JccNonTerminalProduction>? = when (this) {
    is JccJavacodeProduction -> null
    is JccBnfProduction      ->
        mutableSetOf<JccNonTerminalProduction>().takeIf { expansion?.computeLeftMost(it) == true }
    else                     -> null
}

/** Populates the leftmost set of this expansion. */
private fun JccExpansion.computeLeftMost(acc: MutableSet<JccNonTerminalProduction>): Boolean =
        when (this) {
            is JccRegexExpansionUnit         -> true
            is JccScopedExpansionUnit        -> expansionUnit.computeLeftMost(acc)
            is JccAssignedExpansionUnit      -> assignableExpansionUnit?.computeLeftMost(acc) == true
            is JccOptionalExpansionUnit      -> expansion?.computeLeftMost(acc) == true
            is JccParenthesizedExpansionUnit -> expansion?.computeLeftMost(acc) == true
            is JccTryCatchExpansionUnit      -> expansion?.computeLeftMost(acc) == true
            is JccExpansionSequence          -> {
                var isValid = true
                for (unit in expansionUnitList) {
                    val unitIsValid = unit.computeLeftMost(acc)
                    if (!unitIsValid) {
                        isValid = false
                        break
                    } else if (!unit.isEmptyMatchPossible()) {
                        break // valid, don't process the rest
                    }
                }
                isValid
            }
            // breaks early if one returns false
            is JccExpansionAlternative       -> expansionList.all { it.computeLeftMost(acc) }
            // no recursion here. Just local.
            is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction()?.also { acc += it } != null
            else                             -> true // valid, but nothing to do
        }
