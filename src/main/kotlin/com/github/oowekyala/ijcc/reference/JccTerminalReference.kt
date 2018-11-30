package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState

/**
 * Reference to a [JccRegexprSpec].
 * @author Clément Fournier
 * @since 1.0
 */
class JccTerminalReference(psiElement: JccRegularExpressionReference) :
    PsiReferenceBase<JccRegularExpressionReference>(psiElement) {

    private val isRegexContext = psiElement.isInRegexContext()

    override fun resolve(): JccIdentifier? =
            resolveToken()?.regularExpression.let { it as? JccNamedRegularExpression }?.nameIdentifier

    fun resolveToken(): JccRegexprSpec? {
        val searchedName = element.name ?: return null

        val processor = TerminalScopeProcessor(searchedName, isRegexContext)
        val file = element.containingFile
        file.processDeclarations(processor, ResolveState.initial(), element, element)
        return processor.result
    }

    override fun getVariants(): Array<Any> =
            element.containingFile.globalNamedTokens
                .filter { isRegexContext || !it.isPrivate }
                .map { it.name!! }
                .toList()
                .toTypedArray()


    override fun getRangeInElement(): TextRange = element.nameIdentifier.textRangeInParent

    override fun handleElementRename(newElementName: String?): PsiElement = newElementName.toString().let {
        val id = element.nameIdentifier
        JccIdentifierManipulator().handleContentChange(id, newElementName)!!
    }
}