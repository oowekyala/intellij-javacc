package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccJavaCompilationUnit
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * Language injector for the compilation unit.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccLanguageInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> =
        mutableListOf(JccJavaCompilationUnit::class.java, JccNonTerminalProduction::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        when (context) {
            is JccJavaCompilationUnit -> injectIntoCompilationUnit(registrar, context)
            is JccNonTerminalProduction -> injectIntoProduction(registrar, context)
        }
    }

    private fun allOf(psiElement: PsiElement): TextRange = TextRange(0, psiElement.textLength)

    private fun injectIntoCompilationUnit(registrar: MultiHostRegistrar, context: JccJavaCompilationUnit) {
        registrar.startInjecting(JavaLanguage.INSTANCE)
        registrar.addPlace(null, null, context, allOf(context))
        registrar.doneInjecting()
    }

    private fun injectIntoProduction(registrar: MultiHostRegistrar, context: JccNonTerminalProduction) {
        registrar.startInjecting(JavaLanguage.INSTANCE)

        registrar.addPlace("class Dummy {", null, context.header, allOf(context.header))
        registrar.addPlace(null, "}", context.javaBlock, allOf(context.javaBlock))
        registrar.doneInjecting()
    }
}