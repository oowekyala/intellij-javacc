package com.github.oowekyala.ijcc.ide.folding

import com.github.oowekyala.ijcc.ide.folding.JccFoldingBuilder.Companion.FolderVisitor.Companion.BGEN_PATTERN
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.settings.globalPluginSettings
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType


/**
 * Folds code regions not immediately relevant to the grammar,
 * eg parser actions.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccFoldingBuilder : CustomFoldingBuilder() {

    override fun buildLanguageFoldRegions(
        descriptors: MutableList<FoldingDescriptor>,
        root: PsiElement,
        document: Document,
        quick: Boolean
    ) {
        val tmp = mutableListOf<FoldingDescriptor>()

        root.accept(FolderVisitor(tmp))

        // this is not actually an issue but the extra safety may be cool
        val (proper, improper) = tmp.partition { it.range.isProperTextRange() }

        improper.forEach { LOG.error("Improper text range for ${it.element.elementType}: ${it.element.text}") }
        descriptors.clear()
        descriptors.addAll(proper)
    }

    private fun TextRange.isProperTextRange() = startOffset in 0..endOffset

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val opts = globalPluginSettings
        return when (val psi = node.psi) {
            is JccNonTerminalProduction -> false
            is JccOptionSection         -> opts.isFoldOptions
            is JccParserDeclaration     -> opts.isFoldParserDecl
            is JccTokenManagerDecls     -> opts.isFoldTokenMgrDecl
            is JccRegexProduction       -> opts.isFoldTokenProds
            is JccJavaBlock,
            is JccParserActionsUnit     -> opts.isFoldJavaFragments
            is JccLocalLookaheadUnit    -> opts.isFoldLookaheads
            is PsiComment               -> opts.isFoldBgenSections && psi.text.matches(BGEN_PATTERN)
            else                        -> true
        }
    }

    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        return when (val psi = node.psi) {
            is PsiComment                 ->
                when {
                    // start comment of a generated section
                    node.text.matches(BGEN_PATTERN) -> node.text
                    else                            -> "/*...*/"
                }
            is JccTokenReferenceRegexUnit -> literalRegexForRef(psi)!!.stringLiteral.text
            is JccParserDeclaration       -> "/PARSER DECLARATION/"
            is JccTokenManagerDecls       -> "/TOKEN MANAGER DECLARATIONS/"
            is JccRegexProduction         -> "${psi.regexKind.text}: {..}"
            is JccOptionSection           -> "options {..}"
            is JccJavaBlock               -> "{..}"
            is JccParserActionsUnit       -> "{..}"
            is JccLocalLookaheadUnit      -> {
                if (psi.integerLiteral != null && psi.expansion == null && psi.javaExpression == null) {
                    "LOOKAHEAD(${psi.integerLiteral!!.text})"
                } else "LOOKAHEAD(_)" // use one char instead of .. for alignment
            }
            is JccBnfProduction           -> "/BNF ${psi.name}()${psi.jjtreeNodeDescriptor?.text?.let { " $it" } ?: ""}/"
            is JccJavacodeProduction      -> "/JAVACODE ${psi.name}()${psi.jjtreeNodeDescriptor?.text?.let { " $it" } ?: ""}/"
            is JccInjectDirective         -> "INJECT (" + (psi.identifier?.text ?: "???") + ") {..}"
            is JccFullInjectDirective     -> "INJECT {..}"
            else                          -> throw UnsupportedOperationException("Unhandled case $psi")
        }
    }

    companion object {

        private val LOG = Logger.getInstance(JccFoldingBuilder::class.java)


        private fun literalRegexForRef(regexRef: JccTokenReferenceRegexUnit): JccLiteralRegexUnit? =
            regexRef.typedReference.resolveToken()?.asStringToken

        private val newLines = CharArray(2)

        init {
            newLines[0] = '\r'
            newLines[1] = '\n'
        }

        /**
         * Collects all folded regions in the file.
         */
        private class FolderVisitor(val result: MutableList<FoldingDescriptor>) : DepthFirstVisitor() {

            val opts = globalPluginSettings

            // all java blocks in the same bnf production belong in the same group
            private var currentJBlockGroup: FoldingGroup? = null
            // all lookaheads in the same bnf production belong in the same group
            private var currentLookaheadGroup: FoldingGroup? = null
            // all regex productions belong in the same group
            private val regexProductionsGroup = FoldingGroup.newGroup("terminals")

            private val jjtreeGenGroup = FoldingGroup.newGroup("jjtreeGen")

            private var inGenSection: Boolean = false

            override fun visitTokenReferenceRegexUnit(o: JccTokenReferenceRegexUnit) {
                if (!opts.isFoldTokenRefs) return

                val ref = literalRegexForRef(o)
                if (ref != null) {
                    result += FoldingDescriptor(o, o.textRange)
                }
            }

            override fun visitComment(comment: PsiComment) {
                if (comment.text.matches(BGEN_PATTERN)) {
                    val startOffset = comment.textOffset
                    val end = comment.containingFile.text.indexOf(EGEN, startIndex = startOffset + comment.textLength)
                    val endOffset = end + EGEN.length
                    result += FoldingDescriptor(comment.node, TextRange(startOffset, endOffset), jjtreeGenGroup)
                    inGenSection = true
                } else if (comment.text == EGEN) {
                    inGenSection = false
                }

                if (comment.node.elementType != JavaTokenType.END_OF_LINE_COMMENT) {
                    result += FoldingDescriptor(comment, comment.textRange)
                }
            }

            override fun visitRegexProduction(o: JccRegexProduction) {
                result += FoldingDescriptor(o.node, o.textRange, regexProductionsGroup)

                currentJBlockGroup = FoldingGroup.newGroup("tokens:blocks:" + o.name)
                super.visitRegexProduction(o)
                currentJBlockGroup = null
            }

            override fun visitRegexSpec(o: JccRegexSpec) {
                o.lexicalActions?.run {
                    result += FoldingDescriptor(node, textRange, currentJBlockGroup)
                }
            }


            private fun trimWhitespace(o: PsiElement): TextRange {
                var range = o.textRange

                if (o.nextSibling?.node?.elementType == TokenType.WHITE_SPACE
                    && o.nextSibling.textLength > 1
                ) {
                    range = TextRange(range.startOffset, range.endOffset + o.nextSibling.textLength - 1)
                }
                return range
            }

            override fun visitLocalLookaheadUnit(o: JccLocalLookaheadUnit) {
                result += FoldingDescriptor(o.node, trimWhitespace(o), currentLookaheadGroup)
            }

            override fun visitOptionSection(o: JccOptionSection) {
                result += FoldingDescriptor(o, o.textRange)
            }

            override fun visitInjectDirective(o: JccInjectDirective) {
                result += FoldingDescriptor(o, o.textRange)
            }

            override fun visitParserDeclaration(o: JccParserDeclaration) {
                result += FoldingDescriptor(o, o.textRange)
            }

            override fun visitTokenManagerDecls(o: JccTokenManagerDecls) {
                result += FoldingDescriptor(o, o.textRange)
            }

            override fun visitJavacodeProduction(o: JccJavacodeProduction) {
                result += FoldingDescriptor(o.node, o.textRange)
            }


            override fun visitBnfProduction(o: JccBnfProduction) {
                result += FoldingDescriptor(o.node, o.textRange)

                currentJBlockGroup = FoldingGroup.newGroup("bnf:blocks:" + o.name)
                currentLookaheadGroup = FoldingGroup.newGroup("bnf:lookaheads:" + o.name)
                super.visitBnfProduction(o)
                currentJBlockGroup = null
                currentLookaheadGroup = null
            }


            override fun visitParserActionsUnit(unit: JccParserActionsUnit) = visitJavaBlockLike(unit)

            override fun visitJavaBlock(javaBlock: JccJavaBlock) = visitJavaBlockLike(javaBlock)

            private fun visitJavaBlockLike(elt: JccPsiElement) {
                if (elt.textLength > 2) { // not just "{}"
                    result +=
                        when {
                            inGenSection               -> FoldingDescriptor(
                                elt.node,
                                trimWhitespace(elt),
                                jjtreeGenGroup
                            )
                            currentJBlockGroup != null -> FoldingDescriptor(
                                elt.node,
                                trimWhitespace(elt),
                                currentJBlockGroup
                            )
                            else                       -> FoldingDescriptor(elt, trimWhitespace(elt))
                        }
                }
            }

            companion object {
                val BGEN_PATTERN = Regex("""/\*@bgen\(\w++\).*\*/""")
                const val EGEN = "/*@egen*/"

            }
        }
    }
}
