package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.ide.structureview.getPresentationIcon
import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.intellij.codeInsight.TailType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.TailTypeDecorator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * Reference to a named [Token]. Used internally by [LexicalGrammar]
 * when building lexical states so this implementation *cannot use
 * lexical states directly*!! Otherwise we fall into bottomless recursion.
 * But since all named tokens are preserved (no filtering by superseding
 * match is needed like for string tokens), [LexicalGrammar] stores an
 * index of tokens by their name, which is eagerly built on initialisation,
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

    private val canReferencePrivate = referenceUnit.canReferencePrivate

    override fun resolve(): JccIdentifier? =
        resolveToken()?.regularExpression.let { it as? JccNamedRegularExpression }?.nameIdentifier

    fun resolveToken(): Token? {
        val searchedName = element.name ?: return null

        return element.containingFile.lexicalGrammar.getTokenByName(searchedName)
    }

    override fun getVariants(): Array<Any> =
        element.containingFile
            .lexicalGrammar
            .allTokens
            .filter { canReferencePrivate || !it.isPrivate }
            .mapNotNull { token ->
                token.name?.let { name ->
                    LookupElementBuilder
                        .create(name)
                        .withIcon(token.psiElement?.getPresentationIcon())

                }
            }
            .map {
                TailTypeDecorator.withTail(it, TailType.createSimpleTailType('>'))
            }
            .map {
                TailTypeDecorator.withTail(it, TailType.SPACE)
            }
            .toList()
            .toTypedArray()


    override fun getRangeInElement(): TextRange = element.nameIdentifier.textRangeInParent

    override fun handleElementRename(newElementName: String?): PsiElement = newElementName.toString().let {
        val id = element.nameIdentifier
        JccIdentifierManipulator().handleContentChange(id, newElementName)!!
    }
}