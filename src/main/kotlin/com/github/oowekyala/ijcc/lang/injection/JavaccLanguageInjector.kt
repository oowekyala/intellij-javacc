package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.psi.*
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
            is JccJavacodeProduction  -> injectIntoJavacode(registrar, context)
            is JccBnfProduction       -> injectIntoBnf(registrar, context)
        }
    }

    private fun relativeRange(psiElement: PsiElement, from: Int = 0, endOffset: Int = 0): TextRange =
        TextRange(from, psiElement.textLength - endOffset)

    private fun injectIntoCompilationUnit(registrar: MultiHostRegistrar, context: JccJavaCompilationUnit) {
        registrar.startInjecting(JavaLanguage.INSTANCE)
        registrar.addPlace(null, null, context, relativeRange(context))
        registrar.doneInjecting()
    }

    private fun injectIntoJavacode(registrar: MultiHostRegistrar, context: JccJavacodeProduction) {
        registrar.startInjecting(JavaLanguage.INSTANCE)

        registrar.addPlace("class Dummy {", null, context.header, relativeRange(context.header))
        registrar.addPlace(null, "}", context.javaBlock, relativeRange(context.javaBlock))
        registrar.doneInjecting()
    }


    private fun injectIntoBnf(registrar: MultiHostRegistrar, context: JccBnfProduction) {
        registrar.startInjecting(JavaLanguage.INSTANCE, "java")

        // TODO add package + imports + methods from the ACU
        registrar.addPlace("class Dummy {", null, context.header, relativeRange(context.header))

        //  TODO get ast class prefix + package
        val jjtThisTypeName = "AST${context.name}"

        if (context.javaBlock != null) {
            // Add prelude declarations
            registrar.addPlace(
                "{ $jjtThisTypeName jjtThis = new $jjtThisTypeName();",
                null,
                context.javaBlock,
                javaBlockInsides(context.javaBlock)
            )

            if (context.expansionChoices != null)
                JavaBlocksVisitor(registrar).visitExpansionChoices(context.expansionChoices!!)

            registrar.addPlace(null, "}", context.javaBlock, TextRange.EMPTY_RANGE)
        }
        registrar.doneInjecting()
    }

    fun javaBlockInsides(javaBlock: JccJavaBlock): TextRange = relativeRange(javaBlock, 1, 1) // remove braces

    private class JavaBlocksVisitor(private val registrar: MultiHostRegistrar) : JccVisitor() {

        val endBlockBuilder = StringBuilder()
        override fun visitElement(element: PsiElement?) {
            for (child in element!!.children) {
                child.accept(this)
            }
        }

        override fun visitJavaExpression(expr: JccJavaExpression) {
            if (expr.parent is JccLocalLookahead) {
                // then the block represents a boolean expression
                registrar.addPlace(
                    "if (",
                    ");", // FIXME
                    expr,
                    relativeRange(expr)
                )

//                endBlockBuilder.append('}')
            }
        }

        override fun visitJavaBlock(block: JccJavaBlock) {
            registrar.addPlace(null, null, block, relativeRange(block))
        }
    }
}