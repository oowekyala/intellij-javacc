package com.github.oowekyala.ijcc.insight.highlight

import com.github.oowekyala.ijcc.lang.psi.JccIdentifierOwner
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

/**
 * @author Clément Fournier
 * @since 1.0
 */
internal object JccHighlightUtil {

    fun highlightInfo(element: PsiElement,
                      type: HighlightInfoType,
                      severity: HighlightSeverity = type.getSeverity(null),
                      message: String? = null): HighlightInfo =
            highlightInfo(element.textRange, type, severity, message)

    fun highlightInfo(textRange: TextRange,
                      type: HighlightInfoType,
                      severity: HighlightSeverity = type.getSeverity(null), // hack
                      message: String? = null): HighlightInfo =

            HighlightInfo.newHighlightInfo(type)
                .range(textRange)
                .severity(severity)
                .also { if (message != null) it.descriptionAndTooltip(message) }
                .createUnconditionally()

    fun trimWhitespace(psiElement: PsiElement): TextRange {
        var range = psiElement.textRange
        if (psiElement.firstChild.node.elementType == TokenType.WHITE_SPACE) {
            range = range.cutOut(psiElement.firstChild.textRange)
        }

        if (psiElement.lastChild.node.elementType == TokenType.WHITE_SPACE) {
            range = range.cutOut(psiElement.lastChild.textRange)
        }

        return range
    }

    fun wrongReferenceInfo(element: PsiElement, message: String): HighlightInfo =
            wrongReferenceInfo(element.textRange, message)

    fun wrongReferenceInfo(range: TextRange, message: String): HighlightInfo =
            highlightInfo(
                textRange = range,
                severity = HighlightSeverity.ERROR,
                type = HighlightInfoType.WRONG_REF,
                message = message
            )

    fun errorInfo(element: PsiElement, message: String?): HighlightInfo =
            errorInfo(element.textRange, message)

    fun errorInfo(range: TextRange, message: String?): HighlightInfo =
            highlightInfo(
                textRange = range,
                severity = HighlightSeverity.ERROR,
                type = HighlightInfoType.ERROR,
                message = message
            )

    fun checkReference(element: JccIdentifierOwner,
                       normalType: HighlightInfoType): HighlightInfo {
        return if (element.reference?.resolve() == null) {
            wrongReferenceInfo(
                element.nameIdentifier!!, // may not be supported for some elements (eg JjtNodeDescriptor)
                "Unresolved reference: ${element.name}"
            )
        } else {
            highlightInfo(element.nameIdentifier!!, normalType)
        }
    }

}

internal operator fun HighlightInfoHolder.plusAssign(highlightInfo: HighlightInfo) {
    add(highlightInfo)

}