package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.psi.JccJavaBlock
import com.github.oowekyala.ijcc.lang.psi.JccJavacodeProduction
import com.github.oowekyala.ijcc.lang.psi.JccLocalLookahead
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.util.*


/**
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("java code blocks")

        val descriptors = ArrayList<FoldingDescriptor>()

        PsiTreeUtil.findChildrenOfType(root, JccJavaBlock::class.java).forEach {
            if (it.parent !is JccJavacodeProduction && it.parent !is JccLocalLookahead && it.textLength > 2) { // not just "{}"
                descriptors.add(
                    FoldingDescriptor(
                        it.node,
                        TextRange(it.textRange.startOffset + 1, it.textRange.endOffset - 1),
                        group
                    )
                )
            }
        }
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? = "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true

}
