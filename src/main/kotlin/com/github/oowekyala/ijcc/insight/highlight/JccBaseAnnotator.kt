package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.lang.psi.JccIdentifierOwner
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

/**
 * Common utilities for annotators. Annotators should be stateless!
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccBaseAnnotator : Annotator {


    protected fun trimWhitespace(psiElement: PsiElement): TextRange {
        var range = psiElement.textRange
        if (psiElement.firstChild.node.elementType == TokenType.WHITE_SPACE) {
            range = range.cutOut(psiElement.firstChild.textRange)
        }

        if (psiElement.lastChild.node.elementType == TokenType.WHITE_SPACE) {
            range = range.cutOut(psiElement.lastChild.textRange)
        }

        return range
    }

    protected fun AnnotationHolder.highlightOrFlagReference(element: JccIdentifierOwner,
                                                            normalKeys: TextAttributesKey) {
        if (element.reference?.resolve() == null) {
            addHighlight(
                element.nameIdentifier!!, // may not be supported for some elements (eg JjtNodeDescriptor)
                CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES,
                HighlightSeverity.ERROR,
                "Unresolved reference: ${element.name}"
            )
        } else {
            addHighlight(element.nameIdentifier!!, normalKeys)
        }
    }


    protected fun AnnotationHolder.addHighlight(element: PsiElement,
                                                textAttributesKey: TextAttributesKey,
                                                severity: HighlightSeverity = HighlightSeverity.INFORMATION,
                                                message: String? = null) {
        addHighlight(element.textRange, textAttributesKey, severity, message)
    }

    protected fun AnnotationHolder.addHighlight(textRange: TextRange,
                                                textAttributesKey: TextAttributesKey,
                                                severity: HighlightSeverity = HighlightSeverity.INFORMATION,
                                                message: String? = null) {
        val annotation = this.createAnnotation(severity, textRange, message)
        annotation.textAttributes = textAttributesKey
    }
}