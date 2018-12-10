package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccParserDeclaration
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement

/**
 * Language injector for the compilation unit.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccLanguageInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> =
            mutableListOf(JccParserDeclaration::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        when (context) {
            is JccParserDeclaration -> injectIntoFile(registrar, context.containingFile)
        }
    }


    private fun injectIntoFile(registrar: MultiHostRegistrar, jccFile: JccFile) {
        val root = InjectedTreeBuilderVisitor().also { it.visitFile(jccFile) }.nodeStack.pop()
        InjectionRegisterVisitor(registrar).startOn(root)
    }
}