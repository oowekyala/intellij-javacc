package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.ide.structureview.getPresentationIcon
import com.github.oowekyala.ijcc.lang.model.ExplicitToken
import com.github.oowekyala.ijcc.lang.model.SyntheticToken
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
 * Reference to a named [Token].
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

        // This can't use the lexical grammar directly,
        // because the lexical grammar uses references of this type
        // to resolve string tokens
        // TODO you should optimise that, split lexical grammar initialisation in 2

        return element.containingFile
            .grammarFileRoot.allProductions()
            .flatMap { it.tokensUnfiltered() }
            // references can't declare themselves
            .filter { it.regularExpression !is JccRefRegularExpression }
            .firstOrNull { it.name == searchedName }
    }

    private fun JccGrammarFileRoot?.allProductions(): Sequence<JccProduction> =
        this?.childrenSequence()?.filterIsInstance<JccProduction>().orEmpty()

    private fun JccProduction.tokensUnfiltered(): Sequence<Token> {
        return when (this) {
            is JccRegexProduction -> regexSpecList.asSequence().map(::ExplicitToken)

            is JccBnfProduction   ->
                expansion
                    ?.descendantSequence(includeSelf = true)
                    ?.filterIsInstance<JccRegexExpansionUnit>()
                    .orEmpty()
                    .map(::SyntheticToken)

            else                  -> emptySequence()
        }
    }

    override fun getVariants(): Array<Any> =
        element.containingFile.lexicalGrammar
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