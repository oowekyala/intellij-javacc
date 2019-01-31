package com.github.oowekyala.ijcc.insight.cfa

import com.github.oowekyala.ijcc.lang.psi.*


/**
 * Returns true if this expansion can expand to the empty string, returns false otherwise.
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
    is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction().let { it is JccBnfProduction && it.isEmptyMatchPossible() }
    else                             -> false
}

/**
 * Returns true if this BNF production can expand to the empty string, returns false otherwise.
 */
fun JccBnfProduction.isEmptyMatchPossible(): Boolean = expansion?.isEmptyMatchPossible() == true

