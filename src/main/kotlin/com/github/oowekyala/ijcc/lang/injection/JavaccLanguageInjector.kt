package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JavaccPsiElement
import com.github.oowekyala.ijcc.lang.psi.JccGrammarFileRoot
import com.github.oowekyala.ijcc.lang.psi.JccJavaCompilationUnit
import com.github.oowekyala.ijcc.lang.psi.innerRange
import com.github.oowekyala.ijcc.util.runIt
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
        when (context) {
            // FIXME inject both into the same injection file
            is JccJavaCompilationUnit -> registrar.injectIntoJCU(context)
            is JccGrammarFileRoot     -> registrar.injectInto(context)
        }
    }

    private fun MultiHostRegistrar.injectIntoJCU(javaCompilationUnit: JccJavaCompilationUnit) {
        startInjecting(JavaLanguage.INSTANCE)
        addPlace(null, null, javaCompilationUnit, javaCompilationUnit.innerRange())
        doneInjecting()
    }

    private fun MultiHostRegistrar.injectInto(context: JavaccPsiElement) {
        InjectedTreeBuilderVisitor()
            .also { context.accept(it) }
            .nodeStack[0]
            .let {
                when (it) {
                    is JccGrammarFileRoot -> it // already wrapped in the file context
                    else                  -> InjectedTreeBuilderVisitor.wrapInFileContext(context, it)
                }
            }.runIt {
                InjectionRegistrarVisitor(this).startOn(it)
            }
    }
}