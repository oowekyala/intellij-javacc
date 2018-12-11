package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccFile
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
            mutableListOf(JccFile::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        when (context) {
            is JccFile -> injectIntoFile(registrar, context)
        }
    }


    private fun injectIntoFile(registrar: MultiHostRegistrar, jccFile: JccFile) {
        val root = InjectedTreeBuilderVisitor().also { it.visitFile(jccFile) }.nodeStack[0]
        InjectionRegistrarVisitor(registrar).startOn(root)
    }
}