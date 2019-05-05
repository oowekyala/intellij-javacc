package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.github.oowekyala.ijcc.lang.psi.JccJavaCompilationUnit
import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.lang.psi.innerRange
import com.github.oowekyala.ijcc.settings.InjectionSupportLevel.DISABLED
import com.github.oowekyala.ijcc.settings.pluginSettings
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiElement

/**
 * Injects Java into the whole grammar file.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccLanguageInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> =
        mutableListOf(JccJavaCompilationUnit::class.java, JccGrammarFileRoot::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context !is JccPsiElement) return
        if (context.pluginSettings.injectionSupportLevel == DISABLED) return

        when (context) {
            // FIXME inject both into the same injection file
            is JccJavaCompilationUnit -> registrar.injectIntoJCU(context)
            is JccGrammarFileRoot     -> registrar.injectIntoGrammar(context)
        }
    }

    private fun MultiHostRegistrar.injectIntoJCU(jcu: JccJavaCompilationUnit) {
        startInjecting(JavaLanguage.INSTANCE)

        val suffix = InjectedTreeBuilderVisitor.javaccInsertedDecls(
            jcu.containingFile
        ) + "}"

        addPlace(null, suffix, jcu, jcu.innerRange(endOffset = 1)) // remove last brace
        doneInjecting()
    }

    private fun MultiHostRegistrar.injectIntoGrammar(context: JccGrammarFileRoot) {
        context.linearInjectedStructure.register(this)
    }
}
