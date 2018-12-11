package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.pop
import com.intellij.openapi.util.TextRange
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

        // FIXME the compilation unit is not injected in the same injection file as the productions
        // o.parserDeclaration.javaCompilationUnit?.accept(this)

        o.containingFile.nonTerminalProductions.forEach { it.accept(this) }

        mergeTopN(nodeStackImpl.size) { "\n" }
        replaceTop { wrapInFileContext(o, it) }
    }


    // leaves

    private fun visitInjectionHost(o: PsiLanguageInjectionHost,
                                   rangeGetter: (PsiLanguageInjectionHost) -> TextRange = { it.innerRange() }) {
        nodeStackImpl.push(HostLeaf(o, rangeGetter(o)))
    }

    override fun visitJavaAssignmentLhs(o: JccJavaAssignmentLhs) = visitInjectionHost(o)

    override fun visitJavaExpression(o: JccJavaExpression) = visitInjectionHost(o)

    override fun visitJavaBlock(o: JccJavaBlock) = visitInjectionHost(o) {
        it.innerRange(1, 1) // remove the braces
    }

    override fun visitJavaCompilationUnit(o: JccJavaCompilationUnit)  = visitInjectionHost(o)

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


        val args = o.javaExpressionList?.javaExpressionList?.takeIf { it.size > 1 }
            ?: return super.visitNonTerminalExpansionUnit(o)

        args.forEach { visitJavaExpression(it) }

        mergeTopN(args.size) { ", " }
        surroundTop(prefix = "${o.name}(", suffix = ");")
    }

    override fun visitAssignedExpansionUnit(o: JccAssignedExpansionUnit) {

        visitJavaAssignmentLhs(o.javaAssignmentLhs)

        val hasRhs = o.assignableExpansionUnit?.accept(this) != null

        if (hasRhs) {
            mergeTopN(n = 2) { " = " }
        } else {
            surroundTop(prefix = "", suffix = " = ;")
        }
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
            jccNodeClassOwner.nodeQualifiedName?.let { "$it jjtThis = new $it();\n\n" } ?: ""

    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {

        o.expansionUnit.accept(this)

        surroundTop(
            prefix = "{ ${jjtThisDecl(o)}",
            suffix = "}"
        )

    }

    override fun visitBnfProduction(o: JccBnfProduction) {

        visitJavaBlock(o.javaBlock)

        o.expansion?.accept(this) ?: return super.visitBnfProduction(o)

        mergeTopN(n = 2) { "\n" }

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


    companion object {
        private var i = 0
        private fun freshName() = "i${i++}"


        fun wrapInFileContext(element: JavaccPsiElement,
                              node: InjectionStructureTree): InjectionStructureTree {
            val jcu = element.containingFile.parserDeclaration.javaCompilationUnit

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



            return SurroundNode(
                node,
                prefix = commonPrefix + jccDecls,
                suffix = "}"
            )
        }
    }
}