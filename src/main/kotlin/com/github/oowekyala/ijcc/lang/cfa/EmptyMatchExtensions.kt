package com.github.oowekyala.ijcc.lang.cfa

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.foldNullable
import com.github.oowekyala.ijcc.util.takeUntil
import com.intellij.openapi.util.Key
import com.intellij.util.ThreeState
import kotlinx.collections.immutable.*


/**
 * Returns true if this regular expression can match the empty string.
 */
fun JccRegularExpression.isEmptyMatchPossible(): Boolean = computeNullability(persistentListOf())

/**
 * Returns true if this regular expression element can match the empty string.
 */
fun JccRegexElement.isEmptyMatchPossible(): Boolean = isEmptyMatchPossible(persistentListOf())


private fun JccRegularExpression.computeNullability(alreadySeen: PersistentList<JccRegularExpression>): Boolean =
    computeAndCacheNullability(alreadySeen) { getRootRegexElement(followReferences = false)?.isEmptyMatchPossible(it) }

/**
 * Returns true if this production can expand to the empty string.
 */
fun JccNonTerminalProduction.isEmptyMatchPossible(): Boolean = when (this) {
    is JccBnfProduction -> computeNullability(persistentListOf())
    else                -> false
}

/**
 * Returns true if this expansion can expand to the empty string.
 */
fun JccExpansion.isEmptyMatchPossible(): Boolean = isEmptyMatchPossible(persistentListOf())

private fun JccBnfProduction.computeNullability(alreadySeen: PersistentList<JccBnfProduction>): Boolean =
    computeAndCacheNullability(alreadySeen) { expansion?.isEmptyMatchPossible(it) }


private fun JccRegexElement.isEmptyMatchPossible(alreadySeen: PersistentList<JccRegularExpression>): Boolean =
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
            typedReference.resolveToken()?.regularExpression?.computeNullability(alreadySeen) == true
        else                          -> throw IllegalStateException(this.toString())
    }


private fun JccExpansion.isEmptyMatchPossible(alreadySeen: PersistentList<JccBnfProduction>): Boolean =
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
        } == true
        is JccTryCatchExpansionUnit      -> expansion?.isEmptyMatchPossible(alreadySeen) == true
        else                             -> false
    }

typealias LeftMostSet = PersistentSet<JccNonTerminalExpansionUnit>

private fun emptyLeftMostSet(): LeftMostSet = persistentSetOf()

/**
 * Gets the set of productions that this production can expand to without consuming any tokens.
 * This is used to check for left-recursion. If null then either a production reference couldn't
 * be resolved or this is a javacode production.
 */
fun JccNonTerminalProduction.leftMostSet(): LeftMostSet? = when (this) {
    is JccBnfProduction -> expansion?.computeLeftMost() ?: emptyLeftMostSet()
    else                -> null
}

/** Populates the leftmost set of this expansion. Populates the set via side effects.
 *
 * @return True if the result is valid
 */
private fun JccExpansion.computeLeftMost(): LeftMostSet? =
    when (this) {
        is JccRegexExpansionUnit         -> persistentSetOf()
        is JccScopedExpansionUnit        -> expansionUnit.computeLeftMost()
        is JccAssignedExpansionUnit      -> assignableExpansionUnit?.computeLeftMost()
        is JccOptionalExpansionUnit      -> expansion?.computeLeftMost()
        is JccParenthesizedExpansionUnit -> expansion?.computeLeftMost()
        is JccTryCatchExpansionUnit      -> expansion?.computeLeftMost()
        is JccExpansionSequence          ->

            expansionUnitList.asSequence()
                .takeUntil { !it.isEmptyMatchPossible() }
                .map { it.computeLeftMost() }
                .foldNullable(emptyLeftMostSet()) { a, b -> a.addAll(b) }
        is JccExpansionAlternative       ->
            expansionList.asSequence()
                .map { it.computeLeftMost() }
                .foldNullable(emptyLeftMostSet()) { a, b -> a.addAll(b) }

        is JccNonTerminalExpansionUnit   -> persistentSetOf(this)
        else                             -> persistentSetOf() // valid, but nothing to do
    }


private val nullableKey: Key<ThreeState> = Key.create("jcc.bnf.isNullable")

// This caches the nullability status of productions and regexes,
// so as to avoid running in quadratic time, in case of very long
// call chains. This appears to be safe
private fun <T : JccPsiElement> T.computeAndCacheNullability(alreadySeen: PersistentList<T>,
                                                             compute: T.(PersistentList<T>) -> Boolean?): Boolean {

    val isNullable = getUserData(nullableKey) ?: ThreeState.UNSURE

    if (isNullable == ThreeState.UNSURE && this !in alreadySeen) {
        val computed = this.compute(alreadySeen.add(this))
        if (computed != null) {
            putUserData(nullableKey, if (computed) ThreeState.YES else ThreeState.NO)
            return computed
        } // else stays unsure
    }
    return isNullable == ThreeState.YES
}
