package com.github.oowekyala.ijcc

import com.github.oowekyala.ijcc.lang.JavaccTypes.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * @author gark87
 */
class JavaccAnnotator : Annotator {
    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        val node = psiElement.node
        if ((node.elementType !== JCC_REGULAR_EXPRESSION || psiElement.parent.node.elementType !== JCC_EXPANSION_UNIT) && node.elementType !== JCC_COMPLEX_REGULAR_EXPRESSION_UNIT) {
            return
        }
        val firstChild = psiElement.firstChild
        if (firstChild is LeafPsiElement && firstChild.elementType === JCC_LT) {
            val annotation = holder.createInfoAnnotation(node, null)
            annotation.textAttributes = JavaccHighlightingColors.TOKEN.keys
        }
    }
}
