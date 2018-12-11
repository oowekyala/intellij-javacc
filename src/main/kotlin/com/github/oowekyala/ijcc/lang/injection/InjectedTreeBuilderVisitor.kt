package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.pop
import com.intellij.psi.PsiLanguageInjectionHost
import java.util.*

/**
 * Visitor building a [InjectionStructureTree] from a grammar file.
 * The tree is built bottom-up.
 */
class InjectedTreeBuilderVisitor : JccVisitor() {


    private val nodeStackImpl: Deque<InjectionStructureTree> = LinkedList()
    val nodeStack: List<InjectionStructureTree>
        get() = nodeStackImpl.toList()

    // root

    override fun visitGrammarFileRoot(o: JccGrammarFileRoot) {
        // TODO add implicit decls

        o.containingFile.nonTerminalProductions.forEach { it.accept(this) }


        replaceTop(nodeStackImpl.size) {

            val jcu = o.parserDeclaration.javaCompilationUnit

            SurroundNode(
                MultiChildNode(it) { "\n" },
                prefix = jcu?.text?.trim()?.takeIf { it.endsWith("}") }?.removeSuffix("}") ?: "class MyParser {",
                suffix = "}"
            )
        }
    }


    // leaves

    private fun visitInjectionHost(o: PsiLanguageInjectionHost) {
        nodeStackImpl.push(HostLeaf(o))
    }

    override fun visitJavaAssignmentLhs(o: JccJavaAssignmentLhs) = visitInjectionHost(o)

    override fun visitJavaExpression(o: JccJavaExpression) = visitInjectionHost(o)

    override fun visitJavaBlock(o: JccJavaBlock) = visitInjectionHost(o)

    // catch all method, so that the number of leaves
    // corresponds to the number of visited children

    override fun visitJavaccPsiElement(o: JavaccPsiElement) {
        nodeStackImpl.push(EmptyLeaf)
    }

    // control flow tree builders

    override fun visitParserActionsUnit(o: JccParserActionsUnit) {
        visitJavaBlock(o.javaBlock)
    }

    override fun visitNonTerminalExpansionUnit(o: JccNonTerminalExpansionUnit) {

        val args = o.javaExpressionList?.javaExpressionList ?: return super.visitNonTerminalExpansionUnit(o)

        args.forEach { visitJavaExpression(it) }

        mergeTopN(args.size) { ", " }
    }

    override fun visitOptionalExpansionUnit(o: JccOptionalExpansionUnit) {
        val expansion = o.expansion ?: return super.visitOptionalExpansionUnit(o)

        expansion.accept(this)

        surroundTop(
            prefix = "if (/*opt*/${freshName()}()) {",
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
                surroundTop("if (/*?*/${freshName()}()) {", "}")
            }
            is JccOneOrMore, is JccZeroOrMore -> {
                surroundTop("while (/* +* */${freshName()}()) {", "}")
            }
        }
    }

    override fun visitExpansionSequence(o: JccExpansionSequence) {

        val children = o.expansionUnitList

        o.expansionUnitList.forEach { it.accept(this) }

        mergeTopN(children.size) { "/*seq*/\n" }
    }

    override fun visitExpansionAlternative(o: JccExpansionAlternative) {

        val children = o.expansionList

        o.expansionList.forEach { it.accept(this) }

        mergeTopN(children.size) {
            "} else if (/*alt*/${freshName()}()) {"
        }
        surroundTop(
            prefix = "if (${freshName()}()) {",
            suffix = "}"
        )
    }

    override fun visitLocalLookahead(o: JccLocalLookahead) {

        o.javaExpression?.accept(this) ?: return super.visitLocalLookahead(o)

        surroundTop("if /*lookahead*/ (", ");") //fixme?
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
        nodeStackImpl.push(mapper(nodeStackImpl.pop()))
    }

    private fun replaceTop(n: Int, mapper: (List<InjectionStructureTree>) -> InjectionStructureTree) {
        nodeStackImpl.push(mapper(nodeStackImpl.pop(n)))
    }


    private fun mergeTopN(n: Int, delimiter: () -> String) {
        nodeStackImpl.push(MultiChildNode(nodeStackImpl.pop(n), delimiter))
    }


    private companion object {
        private var i = 0
        fun freshName() = "i${i++}"
    }
}