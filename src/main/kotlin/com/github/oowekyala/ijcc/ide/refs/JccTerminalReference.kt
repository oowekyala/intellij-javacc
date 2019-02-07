package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.ide.structureview.getPresentationIcon
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
 * Reference to a [JccRegexSpec].
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
        return element.containingFile.lexicalGrammar.allTokens.firstOrNull { it.name == searchedName }
    }

    override fun getVariants(): Array<Any> =
            element.containingFile.lexicalGrammar
                .allTokens
                .filter { canReferencePrivate || !it.isPrivate }
                .map {
                    LookupElementBuilder
                        .create(it.name!!)
                        .withIcon(it.psiElement?.getPresentationIcon())
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