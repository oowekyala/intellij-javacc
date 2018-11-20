package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

/**
 * Find usages provider.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? = DefaultWordsScanner(
        JavaccLexerAdapter(),
        TokenSet.create(JavaccTypes.JCC_IDENT),
        TokenSet.create(
            JavaccTypes.JCC_END_OF_LINE_COMMENT,
            JavaccTypes.JCC_C_STYLE_COMMENT,
            JavaccTypes.JCC_DOC_COMMENT
        ),
        TokenSet.create(JavaccTypes.JCC_STRING_LITERAL)
    )

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = (element as JccIdentifier).name

    override fun getDescriptiveName(element: PsiElement): String = getNodeText(element, false)

    override fun getType(element: PsiElement): String = when (element.parent) {
        is JccNamedRegularExpression          -> "terminal"
        is JccRegularExpressionReference      -> "terminal" // TODO other cases

        is JccJavaNonTerminalProductionHeader -> "non-terminal"
        is JccNonTerminalExpansionUnit        -> "non-terminal"
        else                                  -> throw IllegalStateException("FIXME: Unhandled context ${element.parent}")
    }

    override fun getHelpId(psiElement: PsiElement): String? = HelpID.FIND_OTHER_USAGES

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is JccIdentifier
}