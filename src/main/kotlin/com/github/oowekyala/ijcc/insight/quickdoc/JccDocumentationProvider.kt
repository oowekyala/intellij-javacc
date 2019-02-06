package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.github.oowekyala.ijcc.util.runIt
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

    private object Log : EnclosedLogger()

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {

        element ?: return null

        val maybeJjtree =
                element as? JccScopedExpansionUnit
                    ?: element
                        .let { it as? JccIdentifier }
                        ?.let { it.parent as? JccJjtreeNodeDescriptor }
                        ?.let { it.parent as? JccScopedExpansionUnit }

        maybeJjtree?.runIt { return JjtNodeDocMaker.makeDoc(it) }

        val maybeProd =
                element as? JccNonTerminalProduction
                    ?: element.parent.parent as? JccNonTerminalProduction // the identifier

        if (maybeProd != null) {
            return when (maybeProd) {
                is JccBnfProduction      -> JccNonTerminalDocMaker.makeDoc(maybeProd)
                is JccJavacodeProduction -> JccNonTerminalDocMaker.makeDoc(maybeProd)
                else                     -> null
            }
        }


        val maybeToken =
                element as? JccRegexprSpec
                    ?: element.let { it as? JccIdentifier }
                        ?.let { it.parent as? JccNamedRegularExpression }
                        ?.let { it.parent as? JccRegexprSpec }


        return maybeToken?.let { JccTerminalDocMaker.makeDoc(it) }
    }


    override fun getDocumentationElementForLink(psiManager: PsiManager?,
                                                link: String?,
                                                context: PsiElement?): PsiElement? =
            JccDocUtil.findLinkTarget(psiManager, link, context)


}