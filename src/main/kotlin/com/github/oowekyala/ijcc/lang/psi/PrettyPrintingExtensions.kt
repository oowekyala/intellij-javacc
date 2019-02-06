package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.plusAssign
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase

/**
 * TODO
 * @author Clément Fournier
 * @since 1.0
 */
fun JccPsiElement.foldingName() = when (this) {
    is JccOptionalExpansionUnit      -> "[...]"
    is JccParenthesizedExpansionUnit -> "(...)" + occurrenceIndicator.foldingName()
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


fun JccOccurrenceIndicator?.foldingName() = this?.text ?: ""

