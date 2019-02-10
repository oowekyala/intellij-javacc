package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.lang.model.ExplicitToken
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.firstOfAnyType
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

    private val stopTypes = arrayOf(
        JccNonTerminalProduction::class.java,
        JccRegexSpec::class.java,
        // stop at the first expansion, the interesting ones are filtered in the "when" stmt
        JccExpansion::class.java
    )

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return element?.ancestors(includeSelf = true)?.firstOfAnyType(*stopTypes)?.let {
            when (it) {
                is JccScopedExpansionUnit -> JjtNodeDocMaker.makeDoc(it)
                is JccBnfProduction       -> JccNonTerminalDocMaker.makeDoc(it)
                is JccJavacodeProduction  -> JccNonTerminalDocMaker.makeDoc(it)
                is JccRegexSpec           -> JccTerminalDocMaker.makeDoc(ExplicitToken(it))
                is JccRegexExpansionUnit  -> it.referencedToken?.let { JccTerminalDocMaker.makeDoc(it) }
                else                      -> null
            }
        }
    }


    override fun getDocumentationElementForLink(psiManager: PsiManager?,
                                                link: String?,
                                                context: PsiElement?): PsiElement? =
        JccDocUtil.findLinkTarget(psiManager, link, context)


}