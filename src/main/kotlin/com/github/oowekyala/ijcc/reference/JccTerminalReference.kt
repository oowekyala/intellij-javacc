package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState

/**
 * Reference to a [JccRegexprSpec].
 * @author Clément Fournier
 * @since 1.0
 */
class JccTerminalReference(psiElement: JccRegularExpressionReference) :
    PsiReferenceBase<JccIdentifier>(psiElement.nameIdentifier) {

    private val isRegexContext = psiElement.isInRegexContext()

    override fun resolve(): PsiElement? {
        val processor = TerminalScopeProcessor(element.name, isRegexContext)
        val file = element.containingFile
        process(processor, file)
        return processor.result
    }

    override fun getVariants(): Array<Any> =
            element.containingFile.globalNamedTokens
                .filter { isRegexContext || !it.isPrivate }
                .map { it.name!! }
                .toList()
                .toTypedArray()

    private fun process(processor: JccBaseIdentifierScopeProcessor, file: JccFile) {
        file.processDeclarations(processor, ResolveState.initial(), element, element)
    }
}