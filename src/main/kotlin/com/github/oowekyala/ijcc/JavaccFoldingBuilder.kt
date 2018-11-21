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
 * Folds code regions not immediately relevant to the grammar,
 * eg parser actions.
 *
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
            is JccJavaBlock                  -> "{..}"
            is JccParserActionsUnit          -> "{..}"
            is JccLocalLookahead             -> {
                if (psi.integerLiteral != null && psi.expansion == null && psi.javaExpression == null) {
                    "LOOKAHEAD(${psi.integerLiteral!!.text})"
                } else "LOOKAHEAD(_)" // use one char instead of .. for alignment
            }
            else                             -> throw UnsupportedOperationException("Unhandled case")
        }
    }

    companion object {

        private fun literalRegexpForRef(regexRef: JccRegularExpressionReference): JccLiteralRegularExpression? {
            return regexRef.reference.resolve()
                .let { it as? JccIdentifier }
                .let { it?.parent as? JccNamedRegularExpression }
                .let { it?.regularExpression as? JccLiteralRegularExpression }
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

            private fun trimWhitespace(o: PsiElement): TextRange {
                var range = o.textRange

                if (o.nextSibling?.node?.elementType == TokenType.WHITE_SPACE) {
                    val ws = o.nextSibling.node.text
                    if (ws.length > 1) {
                        range = range.grown(ws.length - 1)
                    }
                }
                return range
            }

            override fun visitLocalLookahead(o: JccLocalLookahead) {
                result +=
                        if (o.integerLiteral != null && o.expansion == null && o.javaExpression == null)
                            FoldingDescriptor(o.node, trimWhitespace(o), null, emptySet(), true)
                        else
                            FoldingDescriptor(o.node, trimWhitespace(o), currentLookaheadGroup)
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
                // don't fold children
            }


            override fun visitBnfProduction(o: JccBnfProduction) {
                currentJBlockGroup = FoldingGroup.newGroup("bnf:blocks:" + o.name)
                currentLookaheadGroup = FoldingGroup.newGroup("bnf:lookaheads:" + o.name)
                super.visitBnfProduction(o)
                currentJBlockGroup = null
                currentLookaheadGroup = null
            }


            override fun visitParserActionsUnit(unit: JccParserActionsUnit) = visitJavaBlockLike(unit)

            override fun visitJavaBlock(javaBlock: JccJavaBlock) = visitJavaBlockLike(javaBlock)

            private fun visitJavaBlockLike(elt: JavaccPsiElement) {
                if (elt.textLength > 2) { // not just "{}"
                    result += if (currentJBlockGroup != null)
                        FoldingDescriptor(elt.node, trimWhitespace(elt), currentJBlockGroup)
                    else
                        FoldingDescriptor(elt, trimWhitespace(elt))
                }
            }
        }
    }
}
