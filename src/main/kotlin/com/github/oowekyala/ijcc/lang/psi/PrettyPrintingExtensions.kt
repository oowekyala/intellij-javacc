package com.github.oowekyala.ijcc.lang.psi

/**
 * TODO
 * @author Clément Fournier
 * @since 1.0
 */
fun JavaccPsiElement.prettyName() = when (this) {
    is JccOptionalExpansionUnit      -> "[...]"
    is JccParenthesizedExpansionUnit -> "(...)" + occurrenceIndicator.prettyName()
    is JccOptionSection              -> "options {..}"
    is JccParserDeclaration          -> "/PARSER DECLARATION/"
    is JccTokenManagerDecls          -> "/TOKEN MANAGER DECLARATIONS/"
    is JccRegularExprProduction      -> "${regexprKind.text}: {..}"
    is JccLocalLookahead             -> {
        if (isLexical && !isSyntactic && !isSemantic) {
            "LOOKAHEAD($lexicalAmount)"
        } else "LOOKAHEAD(_)" // use one char instead of .. for alignment
    }

    is JccParserActionsUnit          -> "{..}"


    else                             -> text
}

fun JccOccurrenceIndicator?.prettyName() = this?.text ?: ""