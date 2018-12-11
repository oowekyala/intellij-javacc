package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement

/**
 * Injects Java into the whole grammar file.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccLanguageInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> =
            mutableListOf(JccGrammarFileRoot::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        when (context) {
            is JccGrammarFileRoot -> injectIntoFile(registrar, context)
        }
    }


    private fun injectIntoFile(registrar: MultiHostRegistrar, root: JccGrammarFileRoot) {
        val root = InjectedTreeBuilderVisitor().also { it.visitGrammarFileRoot(root) }.nodeStack[0]
        InjectionRegistrarVisitor(registrar).startOn(root)
    }
}