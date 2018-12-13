package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.HostLeaf
import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.github.oowekyala.ijcc.lang.psi.JccJavaCompilationUnit
import com.github.oowekyala.ijcc.lang.psi.innerRange
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

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
        when (context) {
            // FIXME inject both into the same injection file
            is JccJavaCompilationUnit -> registrar.injectIntoJCU(context)
            is JccGrammarFileRoot     -> registrar.injectIntoGrammar(context)
        }
    }

    private fun MultiHostRegistrar.injectIntoJCU(jcu: JccJavaCompilationUnit) {
        startInjecting(JavaLanguage.INSTANCE)

        val suffix = InjectedTreeBuilderVisitor.javaccInsertedDecls(jcu.containingFile) + "}"

        addPlace(null, suffix, jcu, jcu.innerRange(endOffset = 1)) // remove last brace
        doneInjecting()
    }

    private fun MultiHostRegistrar.injectIntoGrammar(context: JccGrammarFileRoot) {
        context.injectionStructureTree.runIt {
            InjectionRegistrarVisitor(this).startOn(it)
        }
    }
}