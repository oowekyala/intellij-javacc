package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveState


/**
 * Reference to a [JccNonTerminalProduction].
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccNonTerminalReference(psiElement: JccIdentifier) :
    PsiReferenceBase<JccIdentifier>(psiElement) {

    override fun resolve(): PsiElement? {
        val processor = NonTerminalScopeProcessor(element.name)
        val file = element.containingFile as JccFileImpl
        process(processor, file)
        return processor.result()
    }

    override fun getVariants(): Array<Any> =
        (element.containingFile as JccFileImpl).nonTerminalProductions.toTypedArray()

    private fun process(processor: JccScopeProcessor, file: JccFileImpl) {
        file.processDeclarations(processor, ResolveState.initial(), element, element)
    }
}