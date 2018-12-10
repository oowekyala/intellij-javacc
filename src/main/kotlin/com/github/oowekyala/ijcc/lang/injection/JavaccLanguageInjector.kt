package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.JavaccLanguageInjector.InjectedTreeNode.*
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.plusAssign
import com.github.oowekyala.ijcc.util.pop
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import java.util.*

/**
 * Language injector for the compilation unit.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccLanguageInjector : MultiHostInjector {
    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> =
            mutableListOf(JccJavaCompilationUnit::class.java, JccJavacodeProduction::class.java)

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
            "class Dummy {${context.header.toJavaMethodHeader()}",
            "}",
            context.javaBlock,
            rangeInside(context.javaBlock)
        )
        registrar.doneInjecting()
    }

    private fun getParserDeclText(jccParserDeclaration: JccParserDeclaration): String {

        return jccParserDeclaration.javaCompilationUnit?.text?.trim()?.removeSuffix("}")
            ?: "class MyParser {"

    }

    private fun injectIntoFile(registrar: MultiHostRegistrar, jccFile: JccFile) {
        registrar.startInjecting(JavaLanguage.INSTANCE, "java")

        // inject into parser decl and productions separately

        val sb = StringBuilder()

        sb += getParserDeclText(jccFile.parserDeclaration)

        for (prod in jccFile.nonTerminalProductions) {


        }

    }


    private fun injectIntoBnf(registrar: MultiHostRegistrar, context: JccBnfProduction) {
        if (context.javaBlock != null) {

            // TODO add package + imports + methods from the ACU
            //        registrar.addPlace("class Dummy {", null, context.header, rangeInside(context.header))

            val config = context.containingFile.javaccConfig
            val prefix = config.nodePrefix


            val jjtThisTypeName = "AST${context.name}"
            // Add prelude declarations
            registrar.addPlace(
                "${context.header.toJavaMethodHeader()}{ $jjtThisTypeName jjtThis = new $jjtThisTypeName();\n",
                "\n",
                context.javaBlock,
                rangeInside(context.javaBlock, 1, 1)
            )

            if (context.expansion != null)
                BnfInjectionVisitor(registrar).visitExpansion(context.expansion!!)

            // skipping lines is important, otherwise the smartkey will jump to the closing brace, which is physically
            // represented at the top of the injection (production header)
            registrar.addPlace(null, "\n\n\n}}", context.javaBlock, TextRange.EMPTY_RANGE)
        }
    }

    private sealed class InjectedTreeNode {

        object EmptyLeaf : InjectedTreeNode()

        data class StringLeaf(val string: String) : InjectedTreeNode()

        data class HostLeaf(val host: PsiLanguageInjectionHost) : InjectedTreeNode()

        data class SurroundNode(val child: InjectedTreeNode,
                                val prefix: String,
                                val suffix: String) : InjectedTreeNode()

        data class MultiChildNode(val children: List<InjectedTreeNode>,
                                  val delimiter: () -> String) : InjectedTreeNode()

    }



    private class InjectedTreeSegmentVisitor : JccVisitor() {
        // build an injected tree


        private companion object {
            private var i = 0
            fun freshName() = "ident${i++}"
        }

        private val nodeStack: Deque<InjectedTreeNode> = LinkedList()

        override fun visitJavaAssignmentLhs(o: JccJavaAssignmentLhs) {
            nodeStack += HostLeaf(o)
        }

        override fun visitJavaExpression(o: JccJavaExpression) {
            nodeStack += HostLeaf(o)
        }

        override fun visitJavaBlock(o: JccJavaBlock) {
            nodeStack += HostLeaf(o)
        }

        // catch all methods
        override fun visitExpansion(o: JccExpansion) {
            nodeStack += EmptyLeaf
        }

        override fun visitJavaccPsiElement(o: JavaccPsiElement) {
            nodeStack += EmptyLeaf
        }

        // control flow tree builders


        override fun visitOptionalExpansionUnit(o: JccOptionalExpansionUnit) {
            val expansion = o.expansion ?: return super.visitOptionalExpansionUnit(o)

            expansion.accept(this)

            surroundTop(
                prefix = "if (${freshName()}()) {",
                suffix = "}"
            )
        }

        override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {

            o.expansion?.accept(this) ?: return super.visitParenthesizedExpansionUnit(o)

            val ind = o.occurrenceIndicator

            when (ind) {
                null /* exactly one */            -> {
                    // do nothing, keep the expansion node on top of the stack
                }
                is JccZeroOrOne                   -> {
                    surroundTop("if (${freshName()}()) {", "}")
                }
                is JccOneOrMore, is JccZeroOrMore -> {
                    surroundTop("while (${freshName()}()) {", "}")
                }
            }
        }

        override fun visitExpansionSequence(o: JccExpansionSequence) {

            val children = o.expansionUnitList

            o.expansionUnitList.forEach { it.accept(this) }

            val nodes = nodeStack.pop(children.size)

            nodeStack += MultiChildNode(nodes, delimiter = { "\n" })
        }

        override fun visitExpansionAlternative(o: JccExpansionAlternative) {

            val children = o.expansionList

            o.expansionList.forEach { it.accept(this) }

            val nodes = nodeStack.pop(children.size)

            val seq = MultiChildNode(nodes, delimiter = { "} else if (${freshName()}()) {" })
            nodeStack += SurroundNode(
                seq,
                prefix = "if (${freshName()}()) {",
                suffix = "}"
            )
        }

        override fun visitLocalLookahead(o: JccLocalLookahead) {

            o.javaExpression?.accept(this) ?: return super.visitLocalLookahead(o)

            surroundTop("if (", ");") //fixme?
        }

        fun jjtThisDecl(jccNodeClassOwner: JccNodeClassOwner): String =
                jccNodeClassOwner.nodeSimpleName?.let { "$it jjtThis = new $it();\n" } ?: ""

        override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {

            o.expansionUnit.accept(this)

            surroundTop(
                prefix = "{ ${jjtThisDecl(o)}",
                suffix = "}"
            )

        }

        override fun visitBnfProduction(o: JccBnfProduction) {

            o.expansion?.accept(this) ?: return super.visitBnfProduction(o)

            surroundTop(
                prefix = "${o.header.toJavaMethodHeader()} {\n ${jjtThisDecl(o)}",
                suffix = "\n}"
            )

        }

        override fun visitJavacodeProduction(o: JccJavacodeProduction) {
            visitJavaBlock(o.javaBlock)

            surroundTop(
                prefix = "${o.header.toJavaMethodHeader()} {\n",
                suffix = "\n}"
            )
        }


        // helper methods

        private fun surroundTop(prefix: String, suffix: String) = replaceTop { SurroundNode(it, prefix, suffix) }

        private fun replaceTop(mapper: (InjectedTreeNode) -> InjectedTreeNode) {
            nodeStack += mapper(nodeStack.pop())
        }

        private fun replaceTop(n: Int, mapper: (List<InjectedTreeNode>) -> InjectedTreeNode) {
            nodeStack += mapper(nodeStack.pop(n))
        }
    }

}