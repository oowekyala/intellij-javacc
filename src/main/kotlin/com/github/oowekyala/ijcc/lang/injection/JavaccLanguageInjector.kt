package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.JavaccPsiElement
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.util.runIt
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
            mutableListOf(JccNonTerminalProduction::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        when (context) {
            is JccNonTerminalProduction ->
                registrar.injectInto(context) {
                    context.wrapWithFileContext(it)
                }
        }
    }

    private fun JavaccPsiElement.wrapWithFileContext(node: InjectionStructureTree): InjectionStructureTree {
        val jcu = containingFile.parserDeclaration.javaCompilationUnit

        // TODO stop ignoring contents of the ACU, in order to modify its structure!
        // TODO add declarations inserted by JJTree (and implements clauses)

        val jccDecls = """
        /** Get the next Token. */
          final public Token getNextToken() {
            // not important
            return null;
          }

        /** Get the specific Token. */
          final public Token getToken(int index) {
            // not important
            return null;
          }
        """.trimIndent()

        val commonPrefix = jcu?.text?.trim()?.takeIf { it.endsWith("}") }?.removeSuffix("}") ?: "class MyParser {"



        return InjectionStructureTree.SurroundNode(
            node,
            prefix = commonPrefix + jccDecls,
            suffix = "}"
        )
    }


    private fun MultiHostRegistrar.injectInto(context: JavaccPsiElement,
                                              transform: (InjectionStructureTree) -> InjectionStructureTree) {
        InjectedTreeBuilderVisitor()
            .also { context.accept(it) }
            .nodeStack[0]
            .let(transform)
            .runIt {
                InjectionRegistrarVisitor(this).startOn(it)
            }
    }
}