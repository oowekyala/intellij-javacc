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

    // Each visit method must push exactly one node on the stack
    // If visiting a node entails visiting other subtrees, these must be merged into a single node
    // If the node may not contain injection hosts, EmptyLeaf must be pushed

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

    override fun visitJavaCompilationUnit(o: JccJavaCompilationUnit) = visitInjectionHost(o)

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


        fun javaccInsertedDecls(file: JccFile) = """
                /** Get the next Token. */
                final public Token getNextToken() {}

                /** Get the specific Token. */
                final public Token getToken(int index) {}

                /** Generate ParseException. */
                public ParseException generateParseException() {}
                private void jj_save(int index, int xla) {}
                private void jj_rescan_token() {}
                private int trace_indent = 0;
                private boolean trace_enabled;

                /** Trace enabled. */
                final public boolean trace_enabled() {}

                /** Enable tracing. */
                final public void enable_tracing() {}

                /** Disable tracing. */
                final public void disable_tracing() {}

                private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
                private int[] jj_expentry;
                private int jj_kind = -1;
                private int[] jj_lasttokens = new int[100];
                private int jj_endpos;

                private void jj_add_error_token(int kind, int pos) {}
                static private final class LookaheadSuccess extends java.lang.Error { }
                final private LookaheadSuccess jj_ls = new LookaheadSuccess();
                private boolean jj_scan_token(int kind) {}
                private Token jj_consume_token(int kind) throws ParseException {}
                public void ReInit(XPathParserTokenManager tm) {}

                /** Generated Token Manager. */
                public ${file.javaccConfig.parserSimpleName}TokenManager token_source;
                /** Current token. */
                public Token token;
                /** Next token. */
                public Token jj_nt;
                private Token jj_scanpos, jj_lastpos;
                private int jj_la;
                private int jj_gen;
                final private int[] jj_la1 = new int[75];
                static private int[] jj_la1_0;
                static private int[] jj_la1_1;
                static private int[] jj_la1_2;
                static private int[] jj_la1_3;
                static {
                   jj_la1_init_0();
                   jj_la1_init_1();
                   jj_la1_init_2();
                   jj_la1_init_3();
                }
                private static void jj_la1_init_0() {
                   jj_la1_0 = new int[] {0x20000000,0x900b4400,0x0,0x20000000,0x40000000,0x0,0x0,0x40000,0x40000,0x1000,0x0,0x4000,0x4000,0x0,0x0,0x2000,0x2000,0x0,0x0,0x0,0x0,0x0,0x0,0x4000,0x4000,0x4000,0x8000,0x90080400,0x900b0400,0x30000,0x30000,0x10000000,0x0,0x10000000,0x0,0x0,0x0,0x0,0x280000,0x280000,0x20000000,0x900b4400,0x900b4400,0x200000,0x80000,0x0,0x80080400,0x0,0x0,0x0,0x20000000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x20000000,0x0,0x0,0x20000000,0x0,0x400,0x400,0x0,0x0,0x80000,0x80000,0x0,0x20000000,0x80000,0x0,0x0,0x0,};
                }
                private static void jj_la1_init_1() {
                   jj_la1_1 = new int[] {0x0,0xffffe3b9,0x0,0x0,0x0,0x4000000,0x8000000,0x0,0x0,0x0,0x0,0x10,0x10,0x70000008,0x70000008,0x80000000,0x80000000,0x0,0x0,0x0,0x0,0x0,0x0,0x10,0x10,0x10,0x0,0xffffe3a9,0xffffe3a9,0x0,0x0,0xffffe009,0x8000,0xffffe009,0x3ff6000,0xffffe008,0xffffe008,0x8,0x0,0x0,0x0,0xffffe3bd,0xffffe3bd,0x0,0x0,0x0,0x3a0,0x380,0x0,0x0,0x0,0x20,0x0,0x0,0x0,0x0,0x8,0x4,0x0,0x8,0x8,0x0,0x8,0x0,0x0,0x1c,0x1c,0x0,0x0,0x0,0x0,0x0,0x4,0xffffe000,0x0,};
                }
                private static void jj_la1_init_2() {
                   jj_la1_2 = new int[] {0x0,0xffffffff,0x4100,0x0,0x200,0x0,0x0,0x10fc,0x10fc,0x0,0x100000,0x0,0x0,0x0,0x0,0x0,0x0,0x3,0x3,0x40000,0x20000,0x10000,0x8000,0x0,0x0,0x0,0x0,0xffffffff,0xffffffff,0x0,0x0,0xffffffff,0x0,0xffffffff,0x0,0xffffffff,0xffffffff,0x0,0x0,0x0,0x0,0xffffffff,0xffffffff,0x0,0x0,0x0,0x0,0x0,0x0,0x800,0x0,0x0,0x800,0xf0000000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0xfc000000,0xf8000000,0x0,0x0,0xfc000000,0x0,0xffffffff,0x0,};
                }
                private static void jj_la1_init_3() {
                   jj_la1_3 = new int[] {0x0,0x7ff,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x7ff,0x7ff,0x0,0x0,0x7ff,0x0,0x7ff,0x0,0x7ff,0x7ff,0x1c0,0x0,0x0,0x0,0x7ff,0x7ff,0x0,0x0,0x620,0x620,0x0,0x620,0x0,0x0,0x0,0x0,0x1f,0x18,0x18,0x600,0x0,0x0,0x600,0x600,0x0,0x600,0x200,0x200,0x0,0x0,0x63f,0x63f,0x20,0x0,0x63f,0x0,0x63f,0x600,};
                }
                final private JJCalls[] jj_2_rtns = new JJCalls[5];
                private boolean jj_rescan = false;
                private int jj_gc = 0;

                static final class JJCalls {
                    int gen;
                    Token first;
                    int arg;
                    JJCalls next;
                }
        """.trimIndent()

        fun wrapInFileContext(element: JavaccPsiElement,
                              node: InjectionStructureTree): InjectionStructureTree {

            val file = element.containingFile
            val jcu = file.parserDeclaration.javaCompilationUnit

            // TODO stop ignoring contents of the ACU, in order to modify its structure!
            // TODO add declarations inserted by JJTree (and implements clauses)


            val commonPrefix = jcu?.text?.trim()?.takeIf { it.endsWith("}") }?.removeSuffix("}") ?: "class MyParser {"

            return SurroundNode(
                node,
                prefix = commonPrefix + javaccInsertedDecls(file),
                suffix = "}"
            )
        }
    }
}