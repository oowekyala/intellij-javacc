package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement

/**
 * Find usages provider.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner? = DefaultWordsScanner(
        JavaccLexerAdapter(),
        JccTypesExt.IdentifierTypeSet,
        JccTypesExt.CommentTypeSet,
        JccTypesExt.StringLiteralTypeSet
    )


    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        (element as JccPsiElement).name ?: "<default name>"

    override fun getDescriptiveName(element: PsiElement): String = getNodeText(element, false)

    override fun getType(element: PsiElement): String {

        return when (element) {
            is JccNonTerminalExpansionUnit -> "non-terminal"
            is JccRegexElement, // TODO should we be more specific?
            is JccRegularExpression,
            is JccRegularExpressionOwner   -> "token"
            else                           -> null
        } ?: "name".also {
            Log { debug("Defaulting type description because unhandled ${element.parent.javaClass.simpleName}") }
        }
    }

    override fun getHelpId(psiElement: PsiElement): String? = HelpID.FIND_OTHER_USAGES

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is JccIdentifier
            || psiElement is JccRegexSpec
            || psiElement is JccNonTerminalProduction
            || psiElement is JccRegexExpansionUnit

    private object Log : EnclosedLogger()
}