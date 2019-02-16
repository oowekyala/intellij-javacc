package com.github.oowekyala.ijcc.ide.quickdoc

import com.github.oowekyala.ijcc.ide.refs.JccLexicalStateReference
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
        JccProduction::class.java,
        JccRegexSpec::class.java,
        JccOptionBinding::class.java,
        // stop at the first expansion, the interesting ones are filtered in the "when" stmt
        JccExpansion::class.java
    )

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {

        val file = element?.containingFile as? JccFile ?: return null

        if (element == file.fakeDefaultStateDecl) {
            return JccLexicalStateDocMaker.makeDoc(file.lexicalGrammar.defaultState)
        } else if (element is JccIdentifier && element.isLexicalStateName) {
            return JccLexicalStateDocMaker.makeDoc(JccLexicalStateReference(element).resolveState()!!)
        }

        val target = (element as? JccIdentifier)?.owner
            ?: element.ancestors(includeSelf = true).firstOfAnyType(*stopTypes)

        return when (target) {
            // TODO use fake psi elements for options
            is JccOptionBinding       -> target.modelOption?.let {
                JccOptionDocMaker.makeDoc(target, target.grammarOptions, it)
            }
            is JccScopedExpansionUnit -> JjtNodeDocMaker.makeDoc(target)
            is JccBnfProduction       -> JccNonTerminalDocMaker.makeDoc(target)
            is JccJavacodeProduction  -> JccNonTerminalDocMaker.makeDoc(target)
            is JccRegexSpec           -> JccTerminalDocMaker.makeDoc(ExplicitToken(target))
            is JccRegexExpansionUnit  -> target.referencedToken?.let { JccTerminalDocMaker.makeDoc(it) }
            else                      -> null
        }
    }

    override fun getDocumentationElementForLink(psiManager: PsiManager?,
                                                link: String?,
                                                context: PsiElement?): PsiElement? =
        JccDocUtil.findLinkTarget(psiManager, link, context)


}