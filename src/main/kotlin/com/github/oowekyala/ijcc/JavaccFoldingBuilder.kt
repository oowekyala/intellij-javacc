package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType


/**
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFoldingBuilder : CustomFoldingBuilder() {

    override fun buildLanguageFoldRegions(
        descriptors: MutableList<FoldingDescriptor>,
        root: PsiElement,
        document: Document,
        quick: Boolean
    ) {
        root.accept(FolderVisitor(descriptors))
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = true

    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        val psi = node.psi
        return when (psi) {
            is JccRegularExpressionReference -> literalRegexpForRef(psi)!!.text
            is JccParserDeclaration          -> "/PARSER DECLARATION/"
            is JccTokenManagerDecls          -> "/TOKEN MANAGER DECLARATIONS/"
            is JccRegularExprProduction      -> "${psi.regexprKind.text}: {..}"
            is JccJavaccOptions              -> "options {..}"
            is JccJavaBlock                  -> " {..}"
            is JccLocalLookahead             -> {
                if (psi.integerLiteral != null && psi.expansionChoices == null && psi.javaExpression == null) {
                    "LOOKAHEAD(${psi.integerLiteral!!.text})"
                } else "LOOKAHEAD(..)"
            }
            else                             -> throw UnsupportedOperationException("Unhandled case")
        }
    }

    companion object {

        private fun <T : Any, R> T?.map(f: (T) -> R?): R? =
            if (this == null) null
            else f(this)

        private fun literalRegexpForRef(regexRef: JccRegularExpressionReference): JccLiteralRegularExpression? {
            return regexRef.reference.resolve()
                .map { it as? JccIdentifier }
                .map { it.parent as? JccNamedRegularExpression }
                .map { it.regularExpression as? JccLiteralRegularExpression }
        }

        private val newLines = CharArray(2)

        init {
            newLines[0] = '\r'
            newLines[1] = '\n'
        }

        private class FolderVisitor(val result: MutableList<FoldingDescriptor>) : JccVisitor() {
            // all java blocks in the same bnf production belong in the same group
            private var currentJBlockGroup: FoldingGroup? = null
            // all lookaheads in the same bnf production belong in the same group
            private var currentLookaheadGroup: FoldingGroup? = null
            // all regex productions belong in the same group
            private val regexpProductionsGroup = FoldingGroup.newGroup("terminals")

            override fun visitElement(o: PsiElement?) {
                o?.children?.forEach { it.accept(this) }
            }

            override fun visitRegularExpressionReference(o: JccRegularExpressionReference) {
                val ref = literalRegexpForRef(o)
                if (ref != null) {
                    result += FoldingDescriptor(o, o.textRange)
                }
            }

            override fun visitRegularExprProduction(o: JccRegularExprProduction) {
                result += FoldingDescriptor(o.node, o.textRange, regexpProductionsGroup)
            }

            fun TextRange.growLeft(delta: Int) = TextRange(startOffset - delta, endOffset)

            private fun trimWhitespace(o: PsiElement, greedyLeft: Boolean = true): TextRange {
                var range = o.textRange
                if (o.prevSibling?.node?.elementType == TokenType.WHITE_SPACE) {
                    val ws = o.prevSibling.node.text

                    range =
                            if (greedyLeft || !ws.startsWith(" ")) {
                                range.union(o.prevSibling.textRange) // eat up all whitespace
                            } else {
                                // keep a space
                                range.growLeft(ws.length - 1)
                            }


                }

                if (o.nextSibling?.node?.elementType == TokenType.WHITE_SPACE) {
                    val ws = o.nextSibling.node.text
                    val fstNewLine = ws.indexOfAny(newLines)
                    if (fstNewLine < 0 && ws.last() == ' ') {
                        range = range.grown(ws.length - 1)
                    } else if (fstNewLine > 0) {
                        range = range.grown(fstNewLine)
                    }
                }
                return range
            }

            override fun visitLocalLookahead(o: JccLocalLookahead) {
                result +=
                        if (o.integerLiteral != null && o.expansionChoices == null && o.javaExpression == null)
                            FoldingDescriptor(o.node, trimWhitespace(o, false), currentLookaheadGroup, emptySet(), true)
                        else
                            FoldingDescriptor(o.node, trimWhitespace(o, false), currentLookaheadGroup)
            }

            override fun visitJavaccOptions(o: JccJavaccOptions) {
                result += FoldingDescriptor(o, o.textRange)
            }

            override fun visitParserDeclaration(o: JccParserDeclaration) {
                result += FoldingDescriptor(o, o.textRange)
            }

            override fun visitTokenManagerDecls(o: JccTokenManagerDecls) {
                result += FoldingDescriptor(o, o.textRange)
            }

            override fun visitJavacodeProduction(o: JccJavacodeProduction) {
                // don't explore children
            }

            override fun visitBnfProduction(o: JccBnfProduction) {
                currentJBlockGroup = FoldingGroup.newGroup("bnf:blocks:" + o.name)
                currentLookaheadGroup = FoldingGroup.newGroup("bnf:lookaheads:" + o.name)
                super.visitBnfProduction(o)
                currentJBlockGroup = null
                currentLookaheadGroup = null
            }

            override fun visitJavaBlock(javaBlock: JccJavaBlock) {
                if (javaBlock.textLength > 2) { // not just "{}"
                    result += if (currentJBlockGroup != null)
                        FoldingDescriptor(javaBlock.node, trimWhitespace(javaBlock), currentJBlockGroup)
                    else
                        FoldingDescriptor(javaBlock, trimWhitespace(javaBlock))
                }
            }
        }
    }
}
