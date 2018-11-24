package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState

/**
 * Reference to a [JccRegexprSpec].
 * @author Clément Fournier
 * @since 1.0
 */
class JccTerminalReference(psiElement: JccIdentifier, private val isRegexContext: Boolean) :
    PsiReferenceBase<JccIdentifier>(psiElement) {

    override fun resolve(): PsiElement? {
        val processor = TerminalScopeProcessor(element.name, isRegexContext)
        val file = element.containingFile
        process(processor, file)
        return processor.result
    }

    override fun getVariants(): Array<Any> {
        val base = if (isRegexContext) element.containingFile.globalNamedTokens
        else element.containingFile.globalPublicNamedTokens

        return base.map { it.name!! }.toList().toTypedArray()
    }

    private fun process(processor: JccBaseIdentifierScopeProcessor, file: JccFile) {
        file.processDeclarations(processor, ResolveState.initial(), element, element)
    }
}