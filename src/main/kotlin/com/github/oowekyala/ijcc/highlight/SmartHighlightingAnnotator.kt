package com.github.oowekyala.ijcc.highlight

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement


/**
 * Complements the syntax highlighting lexer with some syntactic information.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class SmartHighlightingAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is JccJjtreeNodeDescriptor            -> {
                val ident = element.nameIdentifier
                val poundRange = element.firstChild.textRange // "#"
                val fullRange =
                        if (ident != null) poundRange.union(ident.textRange)
                        else
                            element.children
                                .firstOrNull { it.node.elementType == JavaccTypes.JCC_VOID_KEYWORD }
                                ?.let { poundRange.union(it.textRange) }
                                ?: poundRange

                holder.addHighlight(fullRange, JavaccHighlightingColors.JJTREE_DECORATION.keys)
            }
            is JccJavaNonTerminalProductionHeader ->
                holder.addHighlight(element.nameIdentifier, JavaHighlightingColors.METHOD_DECLARATION_ATTRIBUTES)
            is JccRegexprSpec                     ->
                // highlight the name of a global named regex
                element.regularExpression
                    .let { it as? JccNamedRegularExpression }
                    ?.run {
                        holder.addHighlight(nameIdentifier, JavaccHighlightingColors.STRING_TOKEN.keys)
                    }
            is JccLiteralRegularExpression        -> {
                // highlight string literals covered by a regexp spec
                // TODO consider lexical state
                element.reference?.resolve()?.run {
                    holder.addHighlight(element, JavaccHighlightingColors.STRING_TOKEN.keys, "Matched as a token")
                }
            }
        }
    }

    private fun AnnotationHolder.addHighlight(element: PsiElement,
                                              textAttributesKey: TextAttributesKey,
                                              message: String? = null) =
            addHighlight(element.textRange, textAttributesKey, message)

    private fun AnnotationHolder.addHighlight(textRange: TextRange,
                                              textAttributesKey: TextAttributesKey,
                                              message: String? = null) {
        val annotation = this.createInfoAnnotation(textRange, message)
        annotation.textAttributes = textAttributesKey
    }

}