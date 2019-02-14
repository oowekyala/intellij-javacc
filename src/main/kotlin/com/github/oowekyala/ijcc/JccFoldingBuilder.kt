package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
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

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>,
                                          root: PsiElement,
                                          document: Document,
                                          quick: Boolean) {
        val tmp = mutableListOf<FoldingDescriptor>()

        root.accept(FolderVisitor(tmp))

        // this is not actually an issue but the extra safety may be cool
        val (proper, improper) = tmp.partition { it.range.isProperTextRange() }

        improper.forEach { LOG.error("Improper text range for ${it.element.elementType}: ${it.element.text}") }
        descriptors.clear()
        descriptors.addAll(proper)
    }

    private fun TextRange.isProperTextRange() = startOffset in 0..endOffset

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = when (node.psi) {
        is JccNonTerminalProduction -> false
        else                        -> true
    }

    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        val psi = node.psi
        return when (psi) {
            is PsiComment                 -> node.text // start comment of a generated section
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
            is JccBnfProduction           -> "/BNF ${psi.name}()${psi.jjtreeNodeDescriptor?.text?.let { " $it" }
                ?: ""}/"
            is JccJavacodeProduction      -> "/JAVACODE ${psi.name}()${psi.jjtreeNodeDescriptor?.text?.let { " $it" }
                ?: ""}/"
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
            // all java blocks in the same bnf production belong in the same group
            private var currentJBlockGroup: FoldingGroup? = null
            // all lookaheads in the same bnf production belong in the same group
            private var currentLookaheadGroup: FoldingGroup? = null
            // all regex productions belong in the same group
            private val regexProductionsGroup = FoldingGroup.newGroup("terminals")

            private val jjtreeGenGroup = FoldingGroup.newGroup("jjtreeGen")


            override fun visitTokenReferenceRegexUnit(o: JccTokenReferenceRegexUnit) {
                val ref = literalRegexForRef(o)
                if (ref != null) {
                    result += FoldingDescriptor(o, o.textRange)
                }
            }

            override fun visitComment(comment: PsiComment) {
                if (comment.text.matches(BEGEN_PATTERN)) {
                    val startOffset = comment.textOffset
                    val end = comment.containingFile.text.indexOf(EGEN, startIndex = startOffset + comment.textLength)
                    val endOffset = end + EGEN.length
                    result += FoldingDescriptor(comment.node, TextRange(startOffset, endOffset), jjtreeGenGroup)
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
                    result += if (currentJBlockGroup != null)
                        FoldingDescriptor(elt.node, trimWhitespace(elt), currentJBlockGroup)
                    else
                        FoldingDescriptor(elt, trimWhitespace(elt))
                }
            }

            companion object {
                private val BEGEN_PATTERN = Regex("""/\*@bgen\(\w++\).*\*/""")
                private const val EGEN = "/*@egen*/"

            }
        }
    }
}
