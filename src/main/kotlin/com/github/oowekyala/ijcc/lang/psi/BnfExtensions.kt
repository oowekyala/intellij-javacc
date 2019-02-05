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
        // only do this greedy approach if nullable is null
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



