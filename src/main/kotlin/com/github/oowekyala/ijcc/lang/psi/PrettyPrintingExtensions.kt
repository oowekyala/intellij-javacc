package com.github.oowekyala.ijcc.lang.psi

/**
 * TODO
 * @author Clément Fournier
 * @since 1.0
 */
fun JccPsiElement.foldingName() = when (this) {
    is JccOptionalExpansionUnit                                 -> "[...]"
    is JccParenthesizedExpansionUnit                            -> "(...)" + occurrenceIndicator.foldingName()
    is JccOptionSection                                         -> "options {..}"
    is JccParserDeclaration                                     -> "/PARSER DECLARATION/"
    is JccTokenManagerDecls                                     -> "/TOKEN MANAGER DECLARATIONS/"
    is JccRegexProduction                                       -> "${regexKind.text}: {..}"
    is JccLocalLookaheadUnit -> {
        if (isLexical && !isSyntactic && !isSemantic) {
            "LOOKAHEAD($lexicalAmount)"
        } else "LOOKAHEAD(_)" // use one char instead of .. for alignment
    }

    is JccParserActionsUnit                                     -> "{..}"


    else                                                        -> text
}


fun JccOccurrenceIndicator?.foldingName() = this?.text ?: ""

