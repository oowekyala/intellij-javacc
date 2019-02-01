package com.github.oowekyala.ijcc.lang.refs

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.lexer.JavaccLexerAdapter
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
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
        JccTypesExt.IdentifierTypeSet,
        JccTypesExt.CommentTypeSet,
        JccTypesExt.StringLiteralTypeSet
    )

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = (element as PsiNamedElement).name!!

    override fun getDescriptiveName(element: PsiElement): String = getNodeText(element, false)

    override fun getType(element: PsiElement): String {

        val parent = element.parent

        return when (parent) {
            is JccNamedRegularExpression          -> "token"
            is JccJavaNonTerminalProductionHeader -> when {
                parent.parent is JccBnfProduction      -> "BNF production"
                parent.parent is JccJavacodeProduction -> "Javacode production"
                else                                   -> null
            }
            else                                  -> null
        } ?: "name".also {
            Log { debug("Defaulting type description because unhandled ${element.parent.javaClass.simpleName}") }
        }
    }

    override fun getHelpId(psiElement: PsiElement): String? = HelpID.FIND_OTHER_USAGES

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
            psiElement is JccIdentifier || psiElement is JccRegexprSpec || psiElement is JccNonTerminalProduction

    private object Log : EnclosedLogger()
}