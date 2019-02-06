package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState

/**
 * Reference to a [JccRegexSpec].
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccTerminalReference(psiElement: JccTokenReferenceRegexUnit) :
    PsiReferenceBase<JccTokenReferenceRegexUnit>(psiElement) {

    private val canReferencePrivate = psiElement.canReferencePrivate

    override fun resolve(): JccIdentifier? =
            resolveToken()?.regularExpression.let { it as? JccNamedRegularExpression }?.nameIdentifier

    fun resolveToken(): JccRegexSpec? {
        val searchedName = element.name ?: return null

        val processor = TerminalScopeProcessor(searchedName)
        val file = element.containingFile
        file.processDeclarations(processor, ResolveState.initial(), element, element)
        return processor.result
    }

    override fun getVariants(): Array<Any> =
            element.containingFile.globalNamedTokens
                .filter { canReferencePrivate || !it.isPrivate }
                .map { it.name!! }
                .toList()
                .toTypedArray()


    override fun getRangeInElement(): TextRange = element.nameIdentifier.textRangeInParent

    override fun handleElementRename(newElementName: String?): PsiElement = newElementName.toString().let {
        val id = element.nameIdentifier
        JccIdentifierManipulator().handleContentChange(id, newElementName)!!
    }
}