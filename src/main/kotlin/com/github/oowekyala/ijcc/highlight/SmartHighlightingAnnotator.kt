package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.lang.psi.JccJjtreeNodeDescriptor
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement


/**
 * Complements the syntax highlighting lexer with some syntactic information.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class SmartHighlightingAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is JccJjtreeNodeDescriptor) {
            val ident = element.nameIdentifier
            val poundRange = element.firstChild.textRange // "#"
            val fullRange = if (ident != null) poundRange.union(ident.textRange) else poundRange

            val annotation = holder.createInfoAnnotation(fullRange, null)
            annotation.textAttributes = JavaccHighlightingColors.JJTREE_DECORATION.keys
        }
    }
}