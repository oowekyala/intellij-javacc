package com.github.oowekyala.ijcc.lang.psi

import com.intellij.util.ThreeState

/**
 * Extensions to compute properties of BNF productions such as nullability,
 * start and follow set, left-recursivity, etc.
 *
 * @author Clément Fournier
 * @since 1.0
 */


/**
 * Returns true if this expansion can expand to the empty string, returns false otherwise.
 * To determine nullability of a production, use [JccNonTerminalProduction.isNullable] instead,
 * which caches the result.
 */
fun JccExpansion.isEmptyMatchPossible(): Boolean = when (this) {
    is JccParserActionsUnit          -> true
    is JccLocalLookahead             -> true
    is JccOptionalExpansionUnit      -> true
    is JccRegexpExpansionUnit        -> false
    is JccScopedExpansionUnit        -> expansionUnit.isEmptyMatchPossible()
    is JccAssignedExpansionUnit      -> assignableExpansionUnit?.isEmptyMatchPossible() == true
    is JccParenthesizedExpansionUnit -> occurrenceIndicator.let {
        it is JccZeroOrOne || it is JccZeroOrMore
                || expansion?.isEmptyMatchPossible() == true // test it whether there is a + or nothing
    }
    is JccExpansionSequence          -> expansionUnitList.all { it.isEmptyMatchPossible() }
    is JccExpansionAlternative       -> expansionList.any { it.isEmptyMatchPossible() }
    is JccTryCatchExpansionUnit      -> expansion?.isEmptyMatchPossible() == true
    is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction()?.let {
        it is JccBnfProduction && it.computeNullability()
    } ?: false
    else                             -> false
}

private fun JccBnfProduction.computeNullability(): Boolean {
    if (isNullable == ThreeState.UNSURE) {
        val computed = expansion?.isEmptyMatchPossible()
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
            is JccRegexpExpansionUnit        -> true
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
