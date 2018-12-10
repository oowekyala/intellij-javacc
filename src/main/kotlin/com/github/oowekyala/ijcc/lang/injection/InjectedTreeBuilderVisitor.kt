package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.pop
import com.intellij.psi.PsiFile
import java.util.*

/**
 * Visitor building a [InjectionStructureTree] from a grammar file.
 * The tree is built bottom-up.
 */
class InjectedTreeBuilderVisitor : JccVisitor() {


    val nodeStack: Deque<InjectionStructureTree> = LinkedList()

    // root

    override fun visitFile(file: PsiFile?) {
        if (file !is JccFile) return

        // TODO add implicit decls

        file.nonTerminalProductions.forEach { it.accept(this) }


        replaceTop(nodeStack.size) {

            val jcu = file.parserDeclaration.javaCompilationUnit

            SurroundNode(
                MultiChildNode(it) { "\n" },
                prefix = jcu?.text?.trim()?.removeSuffix("}") ?: "class MyParser {",
                suffix = "}"
            )
        }
    }


    // leaves

    override fun visitJavaAssignmentLhs(o: JccJavaAssignmentLhs) {
        nodeStack += HostLeaf(o)
    }

    override fun visitJavaExpression(o: JccJavaExpression) {
        nodeStack += HostLeaf(o)
    }

    override fun visitJavaBlock(o: JccJavaBlock) {
        nodeStack += HostLeaf(o)
    }

    // catch all method, so that the number of leaves
    // corresponds to the number of visited children

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
            null /* exactly one */ -> {
                // do nothing, keep the expansion node on top of the stack
            }
            is JccZeroOrOne -> {
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

        nodeStack += MultiChildNode(
            nodes,
            delimiter = { "\n" })
    }

    override fun visitExpansionAlternative(o: JccExpansionAlternative) {

        val children = o.expansionList

        o.expansionList.forEach { it.accept(this) }

        val nodes = nodeStack.pop(children.size)

        val seq = MultiChildNode(
            nodes,
            delimiter = { "} else if (${freshName()}()) {" })
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

    private fun jjtThisDecl(jccNodeClassOwner: JccNodeClassOwner): String =
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

    private fun surroundTop(prefix: String, suffix: String) =
            replaceTop { SurroundNode(it, prefix, suffix) }

    private fun replaceTop(mapper: (InjectionStructureTree) -> InjectionStructureTree) {
        nodeStack += mapper(nodeStack.pop())
    }

    private fun replaceTop(n: Int, mapper: (List<InjectionStructureTree>) -> InjectionStructureTree) {
        nodeStack += mapper(nodeStack.pop(n))
    }


    private companion object {
        private var i = 0
        fun freshName() = "ident${i++}"
    }
}