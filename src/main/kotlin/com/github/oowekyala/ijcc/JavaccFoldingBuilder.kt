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
import java.util.*


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
            val decl = regexRef.reference.resolve()
            if (decl is JccIdentifier && decl.parent is JccNamedRegularExpression) {
                val parent = decl.parent as JccNamedRegularExpression
                return parent.complexRegexpChoices
                    ?.complexRegexpSequenceList
                    ?.takeIf { it.size == 1 }
                    ?.get(0)
                    ?.complexRegexpUnitList
                    ?.takeIf { it.size == 1 }
                    ?.get(0)
                    .map { it.literalRegularExpression }
            }
            return null
        }

        private class FolderVisitor(val result: MutableList<FoldingDescriptor>) : JccVisitor() {
            // all java blocks in the same bnf production belong in the same group
            private val currentGroup = Stack<FoldingGroup>()
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

            override fun visitLocalLookahead(o: JccLocalLookahead) {
                result += FoldingDescriptor(o, o.textRange)
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
                currentGroup += FoldingGroup.newGroup("bnf:" + o.name)
                super.visitBnfProduction(o)
                currentGroup.pop()
            }

            override fun visitJavaBlock(javaBlock: JccJavaBlock) {
                if (javaBlock.textLength > 2) { // not just "{}"
                    var range = javaBlock.textRange
                    if (javaBlock.prevSibling.node.elementType == TokenType.WHITE_SPACE) {
                        range = range.union(javaBlock.prevSibling.textRange)
                    }

                    result += if (!currentGroup.isEmpty())
                        FoldingDescriptor(javaBlock.node, range, currentGroup.peek())
                    else
                        FoldingDescriptor(javaBlock, range)
                }
            }


        }
    }
}
