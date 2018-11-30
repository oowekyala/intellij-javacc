package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * Documentation extension point.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JccDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val relevantNode = element?.parentSequence(includeSelf = true)
            ?.first {
                it is JccRegexprSpec
                        || it is JccNamedRegularExpression
                        || it is JccNonTerminalProduction
            }
            ?: return null

        return when (relevantNode) {
            is JccNamedRegularExpression -> JccTerminalDocMaker.makeDoc(relevantNode)
            is JccBnfProduction          -> JccNonTerminalDocMaker.makeDoc(relevantNode)
            is JccJavacodeProduction     -> JccNonTerminalDocMaker.makeDoc(relevantNode)
            else                         -> null
        }
    }


    override fun getDocumentationElementForLink(psiManager: PsiManager?,
                                                link: String?,
                                                context: PsiElement?): PsiElement? =
            JccDocUtil.findLinkTarget(psiManager, link, context)


}