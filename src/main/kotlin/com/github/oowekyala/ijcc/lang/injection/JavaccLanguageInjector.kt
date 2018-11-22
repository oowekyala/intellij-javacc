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

    private fun rangeInside(psiElement: PsiElement, from: Int = 0, endOffset: Int = 0): TextRange =
        TextRange(from, psiElement.textLength - endOffset)

    private fun injectIntoCompilationUnit(registrar: MultiHostRegistrar, context: JccJavaCompilationUnit) {
        registrar.startInjecting(JavaLanguage.INSTANCE)
        registrar.addPlace(null, null, context, rangeInside(context))
        registrar.doneInjecting()
    }

    private fun injectIntoJavacode(registrar: MultiHostRegistrar, context: JccJavacodeProduction) {
        registrar.startInjecting(JavaLanguage.INSTANCE)

        //        registrar.addPlace("", null, context.header, rangeInside(context.header))
        registrar.addPlace(
            "class Dummy {${context.header?.toJavaMethodHeader()}",
            "}",
            context.javaBlock,
            rangeInside(context.javaBlock)
        )
        registrar.doneInjecting()
    }


    private fun injectIntoBnf(registrar: MultiHostRegistrar, context: JccBnfProduction) {
        if (context.javaBlock != null) {
            registrar.startInjecting(JavaLanguage.INSTANCE, "java")

            // TODO add package + imports + methods from the ACU
            //        registrar.addPlace("class Dummy {", null, context.header, rangeInside(context.header))

            //  TODO get ast class prefix + package
            val jjtThisTypeName = "AST${context.name}"
            // Add prelude declarations
            registrar.addPlace(
                "class Dummy{ ${context.header.toJavaMethodHeader()}{ $jjtThisTypeName jjtThis = new $jjtThisTypeName();\n",
                "\n",
                context.javaBlock,
                rangeInside(context.javaBlock, 1, 1)
            )

            if (context.expansion != null)
                BnfInjectionVisitor(registrar).visitExpansion(context.expansion!!)

            // skipping lines is important, otherwise the smarkey will jump to the closing brace, which is physically
            // represented at the top of the injection (production header)
            registrar.addPlace(null, "\n\n\n}}", context.javaBlock, TextRange.EMPTY_RANGE)
            registrar.doneInjecting()
        }
    }

    fun javaBlockInsides(javaBlock: JccJavaBlock): TextRange = rangeInside(javaBlock, 1, 1) // remove braces

    private class BnfInjectionVisitor(private val registrar: MultiHostRegistrar) : JccVisitor() {
        companion object {
            private var i = 0
            fun freshName() = "ident${i++}"
        }

        val endBlockBuilder = StringBuilder()

        override fun visitElement(element: PsiElement?) {
            for (child in element!!.children) {
                child.accept(this)
            }
        }

        override fun visitJavaAssignmentLhs(o: JccJavaAssignmentLhs) {
            registrar.addPlace(null, "= getToken(0);", o, rangeInside(o))
        }
        override fun visitJavaExpression(expr: JccJavaExpression) {
            //            if (expr.parent is JccLocalLookahead) {
            //                 then the block represents a boolean expression
            //                registrar.addPlace(
            //                    "if (",
            //                    ");", // FIXME
            //                    expr,
            //                    rangeInside(expr)
            //                )
            //                                endBlockBuilder.append('}')
            //            } else {
            registrar.addPlace("\nObject ${freshName()} = ", ";", expr, rangeInside(expr))
            //            }
        }

        override fun visitJavaBlock(block: JccJavaBlock) {
            registrar.addPlace("\n", "\n", block, rangeInside(block, 1, 1))
        }
    }
}