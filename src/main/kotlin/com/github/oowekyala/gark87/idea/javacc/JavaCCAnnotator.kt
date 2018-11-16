package com.github.oowekyala.gark87.idea.javacc

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * @author gark87
 */
class JavaCCAnnotator : Annotator {
    override fun annotate(psiElement: PsiElement, holder: AnnotationHolder) {
        val node = psiElement.node
        if ((node.elementType !== JavaCCTreeConstants.JJTREGULAR_EXPRESSION || psiElement.parent.node.elementType !== JavaCCTreeConstants.JJTEXPANSION_UNIT) && node.elementType !== JavaCCTreeConstants.JJTCOMPLEX_REGULAR_EXPRESSION_UNIT) {
            return
        }
        val firstChild = psiElement.firstChild
        if (firstChild is LeafPsiElement && firstChild.elementType === JavaCCConstants.LT) {
            val annotation = holder.createInfoAnnotation(node, null)
            annotation.textAttributes = JavaCCHighlighter.TOKEN
        }
    }
}
