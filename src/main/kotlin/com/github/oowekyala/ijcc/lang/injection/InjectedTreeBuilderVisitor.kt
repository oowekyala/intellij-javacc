package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.parserSimpleName
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.pop
import com.intellij.ide.structureView.impl.java.JavaClassTreeElementBase
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import java.util.*

/**
 * Visitor building a [InjectionStructureTree] from a grammar file.
 * The tree is built bottom-up.
 */
class InjectedTreeBuilderVisitor private constructor() : JccVisitor() {

    // Each visit method must push exactly one node on the stack
    // If visiting a node entails visiting other subtrees, these must be merged into a single node
    // If the node may not contain injection hosts, EmptyLeaf must be pushed

    private val nodeStackImpl: Deque<InjectionStructureTree> = ArrayDeque()
    val nodeStack: List<InjectionStructureTree>
        get() = nodeStackImpl.toList()

    private var i = 0
    private fun freshName() = "i${i++}"

    // root

    override fun visitGrammarFileRoot(o: JccGrammarFileRoot) {

        // FIXME the compilation unit is not injected in the same injection file as the productions
//        o.parserDeclaration.javaCompilationUnit?.accept(this)

        o.containingFile.nonTerminalProductions.forEach { it.accept(this) }

        mergeTopN(nodeStackImpl.size) { "\n" }
        replaceTop { wrapInFileContext(o, it) }
    }


    // leaves

    private fun visitInjectionHost(o: PsiLanguageInjectionHost,
                                   rangeGetter: (PsiLanguageInjectionHost) -> TextRange = { it.innerRange() }) {
        nodeStackImpl.push(HostLeaf(o, rangeGetter))
    }

    override fun visitJavaAssignmentLhs(o: JccJavaAssignmentLhs) = visitInjectionHost(o)

    override fun visitJavaExpression(o: JccJavaExpression) = visitInjectionHost(o)

    override fun visitJavaBlock(o: JccJavaBlock) = visitInjectionHost(o) {
        it.innerRange(1, 1) // remove the braces
    }

//    override fun visitJavaCompilationUnit(o: JccJavaCompilationUnit) = visitInjectionHost(o)

    // catch all method, so that the number of leaves
    // corresponds to the number of visited children

    override fun visitElement(o: PsiElement) {
        nodeStackImpl.push(EmptyLeaf)
    }

    // control flow tree builders

    override fun visitParserActionsUnit(o: JccParserActionsUnit) {
        visitJavaBlock(o.javaBlock)
    }

    override fun visitRegexExpansionUnit(o: JccRegexExpansionUnit) {
        if (o.parent is JccAssignedExpansionUnit) {
            pushStringLeaf("getToken(1)")
        } else super.visitRegexExpansionUnit(o)
    }

    override fun visitNonTerminalExpansionUnit(o: JccNonTerminalExpansionUnit) {

        val args = o.javaExpressionList?.javaExpressionList?.takeIf { it.size > 0 }
            ?: run {
                pushStringLeaf(o.name + "()")
                return
            }

        args.forEach { visitJavaExpression(it) }

        mergeTopN(args.size) { ", " }
        surroundTop(prefix = "${o.name}(", suffix = ")")
    }

    override fun visitAssignedExpansionUnit(o: JccAssignedExpansionUnit) {

        visitJavaAssignmentLhs(o.javaAssignmentLhs)

        val hasRhs = o.assignableExpansionUnit?.accept(this) != null

        if (hasRhs) {
            mergeTopN(n = 2) { " = " }
            surroundTop(prefix = "", suffix = ";")
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

    override fun visitLocalLookaheadUnit(o: JccLocalLookaheadUnit) {

        o.javaExpression?.accept(this) ?: return super.visitLocalLookaheadUnit(o)

        surroundTop("if /*lookahead*/ (", ");") //fixme?
    }

    private fun jjtThisDecl(jjtNodeClassOwner: JjtNodeClassOwner): String =
        jjtNodeClassOwner.nodeQualifiedName?.let { "$it jjtThis = new $it();\n\n" } ?: ""

    override fun visitJjtreeNodeDescriptorExpr(o: JccJjtreeNodeDescriptorExpr) {
        visitJavaExpression(o.javaExpression)

        if (o.isGtExpression) {
            surroundTop(prefix = "jjtree.arity() > ", suffix = "")
        }
    }

    override fun visitJjtreeNodeDescriptor(o: JccJjtreeNodeDescriptor) {

        o.descriptorExpr?.accept(this) ?: return super.visitJjtreeNodeDescriptor(o)

        surroundTop(prefix = "jjtree.closeNodeScope(jjtThis, ", suffix = ");")
    }

    override fun visitScopedExpansionUnit(o: JccScopedExpansionUnit) {

        o.expansionUnit.accept(this)
        o.jjtreeNodeDescriptor.accept(this)

        mergeTopN(n = 2) { "\n" }
        surroundTop(
            prefix = "{ ${jjtThisDecl(o)}", // this is technically not valid java but it's ok
            suffix = "}"
        )

    }

    override fun visitNonTerminalProduction(o: JccNonTerminalProduction) {
        o.javaBlock?.accept(this) ?: return super.visitNonTerminalProduction(o)

        val baseNumChildren = when (o) {
            is JccBnfProduction -> {
                o.expansion?.accept(this) ?: return super.visitNonTerminalProduction(o)
                2
            }
            else                -> 1
        }

        val totalNumChildren = when (o.jjtreeNodeDescriptor?.accept(this)) {
            null -> baseNumChildren // the node descriptor doesn't exist
            else -> baseNumChildren + 1
        }


        mergeTopN(totalNumChildren) { "\n" }

        surroundTop(
            prefix = "${o.header.toJavaMethodHeader()} {\n ${jjtThisDecl(o)}",
            suffix = "\n}"
        )

        // prevent suffix leak
        replaceTop { StructuralBoundary(it) }

    }

    // helper methods


    /** Merge the [n] nodes on top of the stack into a [MultiChildNode], delimited by [delimiter]. */
    private fun mergeTopN(n: Int, delimiter: () -> String) = replaceTop(n) { MultiChildNode(it, delimiter) }

    /** Surrounds the node on top of the stack with a [SurroundNode], using the given [prefix] and [suffix]. */
    private fun surroundTop(prefix: String, suffix: String) = replaceTop { SurroundNode(it, prefix, suffix) }

    /** Pushes the equivalent of a leaf holding the given text on the stack. */
    private fun pushStringLeaf(text: String) {
        nodeStackImpl.push(SurroundNode(EmptyLeaf, prefix = text, suffix = ""))
    }


    private inline fun replaceTop(mapper: (InjectionStructureTree) -> InjectionStructureTree) {
        nodeStackImpl.push(mapper(nodeStackImpl.pop()))
    }

    private inline fun replaceTop(n: Int, mapper: (List<InjectionStructureTree>) -> InjectionStructureTree) {
        nodeStackImpl.push(mapper(nodeStackImpl.pop(n)))
    }


    companion object {

        val generatedFieldNames = setOf(
            "trace_indent",
            "trace_enabled",
            "token_source",
            // "jjtree" // show this, as it can be useful
        )
        val generatedMethodNames = setOf(
            "getNextToken",
//            "getToken(int)",
            "generateParseException",
            "trace_enabled",
            "enable_tracing",
            "disable_tracing",
            "ReInit",
        )
        val generatedClassNames = setOf("LookaheadSuccess", "JJCalls")

        fun isGeneratedMember(javaTree: JavaClassTreeElementBase<*>): Boolean {
            return when (val elt = javaTree.element) {
                is PsiField -> elt.name.startsWith("jj_") || elt.name in generatedFieldNames
                is PsiMethod ->
                    elt.name.startsWith("jj_")
                            || elt.name in generatedMethodNames && elt.parameterList.parametersCount == 0
                            || elt.name == "getToken" && elt.parameterList.let { it.parametersCount == 1 && it.getParameter(
                        0
                    )!!.type.getPresentableText(false) == "int" }
                is PsiClass -> elt.name in generatedClassNames
                else -> false
            }
        }

        /** Gets the injection subtree for the given node. */
        fun getInjectedSubtreeFor(node: JccPsiElement): InjectionStructureTree =
            InjectedTreeBuilderVisitor()
                .also { node.accept(it) }
                .nodeStack[0]

        // todo should be private
        fun javaccInsertedDecls(file: JccFile): String {
            val parserName = file.grammarOptions.parserSimpleName
            val isJjtree = file.grammarNature >= GrammarNature.JJTREE

            val jjtreeDecls = if (isJjtree) """
                 /** Only available in JJTree grammars. */
                 protected JJT${parserName}State jjtree = new JJT${parserName}State();
            """.trimIndent()
            else ""

            return """
                        $jjtreeDecls

                        /**
                         * Returns the token at the given index (0 means last consumed token, 1 is the token just in front of us). 
                         */
                        final public Token getToken(int index) {}

                        /** @deprecated Use {@link #getToken(int) getToken(0)} */
                        @Deprecated
                        public Token token;
                        /* Returns the next token. */
                        @Deprecated
                        final public Token getNextToken() {}

                        /** Generate a parse exception. */
                        public ParseException generateParseException() {}

                        public void ReInit(${parserName}TokenManager tm) {}

                        /** Generated Token Manager. */
                        public ${parserName}TokenManager token_source;

                        /* This is hidden, as it's the internals of the parser.
                        public Token jj_nt;
                        private Token jj_scanpos, jj_lastpos;
                        private int jj_la;
                        private int jj_gen;
                        final private int[] jj_la1 = new int[75];
                        static private int[] jj_la1_0;
                        static private int[] jj_la1_1;
                        static private int[] jj_la1_2;
                        static private int[] jj_la1_3;
                        private static void jj_la1_init_0() {}
                        private static void jj_la1_init_1() {}
                        private static void jj_la1_init_2() {}
                        private static void jj_la1_init_3() {}
                        final private JJCalls[] jj_2_rtns = new JJCalls[5];
                        private boolean jj_rescan = false;
                        private int jj_gc = 0;

                        final private LookaheadSuccess jj_ls = new LookaheadSuccess();
                        private boolean jj_scan_token(int kind) {}
                        private Token jj_consume_token(int kind) throws ParseException {}

                        private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
                        private int[] jj_expentry;
                        private int jj_kind = -1;
                        private int[] jj_lasttokens = new int[100];
                        private int jj_endpos;

                        private void jj_save(int indices, int xla) {}
                        private void jj_rescan_token() {}

                        private void jj_add_error_token(int kind, int pos) {}
                        static private final class LookaheadSuccess extends java.lang.Error { }

                        static final class JJCalls {
                            int gen;
                            Token first;
                            int arg;
                            JJCalls next;
                        }
                        */
                """.trimIndent()
        }

        fun wrapInFileContext(element: JccPsiElement,
                              node: InjectionStructureTree): InjectionStructureTree {

            val file = element.containingFile
            val jcu = file.parserDeclaration?.javaCompilationUnit


            val leaf =

                if (jcu != null && jcu.text.isBlank().not())
                    SurroundNode(
                        MultiChildNode(
                            children = listOf(
                                HostLeaf(jcu) { ElementManipulators.getManipulator(it).getRangeInElement(it) },
                                node
                            )
                        ) { "" },
                        prefix = "",
                        suffix = "}"
                    )
                else {
                    SurroundNode(node, prefix = "class SomeParser {" + javaccInsertedDecls(file), suffix = "}")
                }


            return SurroundNode(
                leaf,
                prefix = "",
                suffix = javaccInsertedDecls(file) + "}"
            )
        }


        // TODO stop ignoring contents of the ACU, in order to modify its structure!
        // TODO add declarations inserted by JJTree (and implements clauses)

    }
}
