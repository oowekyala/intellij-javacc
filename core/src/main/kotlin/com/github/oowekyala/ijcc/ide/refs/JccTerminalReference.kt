package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * Reference to a named [Token]. Used internally by [LexicalGrammar]
 * when building lexical states so this implementation *cannot use
 * lexical states directly*!! Otherwise we fall into bottomless recursion.
 * But since all named tokens are preserved (no filtering by superseding
 * match is needed like for string tokens), [LexicalGrammar] stores an
 * indices of tokens by their name, which is eagerly built on initialisation,
 * and allows this reference to resolve its result very fast ([LexicalGrammar.getTokenByName]).
 *
 * This is a *major* performance optimisation that alone lets the plugin
 * open files like PlDocAst.jjt quickly instead of hanging.
 *
 * Compare 61d4d0a with e71558c.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccTerminalReference(referenceUnit: JccTokenReferenceRegexUnit) :
    PsiReferenceBase<JccTokenReferenceRegexUnit>(referenceUnit) {

    // doesn't need to query the lexical grammar
    // used by find usages
    override fun isReferenceTo(target: PsiElement): Boolean =
        target is JccRegexSpec && target.name == element.name
            || target is JccRegexExpansionUnit
            && target.regularExpression.let { it is JccNamedRegularExpression && it.name == element.name }

    /**
     * Returns the element underlying the [Token] returned by [resolveToken].
     */
    override fun resolve(): JccRegularExpressionOwner? = resolveToken()?.psiElement

    fun resolveToken(): Token? = element.name?.let {
        element.containingFile.lexicalGrammar.getTokenByName(it)
    }

    override fun getVariants(): Array<Any> =
        JccRefVariantService.getInstance().terminalVariants(this)


    override fun getRangeInElement(): TextRange = element.nameIdentifier.textRangeInParent

    override fun handleElementRename(newElementName: String): PsiElement = newElementName.let {
        val id = element.nameIdentifier
        JccIdentifierManipulator().handleContentChange(id, newElementName)!!
    }
}
