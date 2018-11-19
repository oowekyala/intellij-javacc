package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.JccJavaBlock
import com.github.oowekyala.ijcc.lang.psi.JccJavacodeProduction
import com.github.oowekyala.ijcc.lang.psi.JccLocalLookahead
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil


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

        PsiTreeUtil.findChildrenOfAnyType(root, JccLocalLookahead::class.java, JccJavaBlock::class.java).forEach {
            val descriptor = when (it) {
                is JccJavaBlock      -> getDescriptorForJavaBlock(it)
                is JccLocalLookahead -> getDescriptorForLookahead(it)
                else                 -> null
            }

            if (descriptor != null) descriptors.add(descriptor)

        }
    }


    private fun getDescriptorForLookahead(lookahead: JccLocalLookahead): FoldingDescriptor? {
        return FoldingDescriptor(
            lookahead,
            lookahead.textRange
        )
    }


    private fun getDescriptorForJavaBlock(javaBlock: JccJavaBlock): FoldingDescriptor? {
        if (javaBlock.parent !is JccJavacodeProduction && javaBlock.textLength > 2) { // not just "{}"
            var range = javaBlock.textRange
            if (javaBlock.prevSibling.node.elementType == TokenType.WHITE_SPACE) {
                range = range.union(javaBlock.prevSibling.textRange)
            }

            return FoldingDescriptor(
                javaBlock,
                range
            )
        }
        return null
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean = true

    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        val psi = node.psi
        return when (psi) {
            is JccJavaBlock -> " {...}"
            is JccLocalLookahead -> {
                if (psi.integerLiteral != null && psi.expansionChoices == null && psi.javaExpression == null) {
                    "LOOKAHEAD(${psi.integerLiteral!!.text})"
                } else "LOOKAHEAD(...)"
            }
            else -> throw UnsupportedOperationException("Unhandled case")
        }
    }
}
