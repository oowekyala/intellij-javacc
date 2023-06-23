package com.github.oowekyala.ijcc.ide.highlight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccHighlightUtil {

    fun highlightInfo(
        element: PsiElement,
        type: HighlightInfoType,
        severity: HighlightSeverity = type.getSeverity(null),
        message: String? = null
    ): HighlightInfo.Builder =
        highlightInfo(element.textRange, type, severity, message)

    fun highlightInfo(
        textRange: TextRange,
        type: HighlightInfoType,
        severity: HighlightSeverity = type.getSeverity(null), // hack
        message: String? = null
    ): HighlightInfo.Builder =

        HighlightInfo.newHighlightInfo(type)
            .range(textRange)
            .severity(severity)
            .let {
                if (message != null) it.descriptionAndTooltip(message)
                else it
            }

    fun wrongReferenceInfo(
        element: PsiElement,
        message: String
    ): HighlightInfo.Builder =
        wrongReferenceInfo(element.textRange, message)

    fun wrongReferenceInfo(
        range: TextRange,
        message: String
    ): HighlightInfo.Builder =
        highlightInfo(
            textRange = range,
            severity = HighlightSeverity.ERROR,
            type = HighlightInfoType.WRONG_REF,
            message = message
        )

    fun warningInfo(
        element: PsiElement,
        message: String
    ): HighlightInfo.Builder =
        highlightInfo(
            textRange = element.textRange,
            severity = HighlightSeverity.WARNING,
            type = HighlightInfoType.WARNING,
            message = message
        )

    fun errorInfo(
        element: PsiElement,
        message: String?
    ): HighlightInfo.Builder =
        errorInfo(element.textRange, message)

    fun errorInfo(
        range: TextRange,
        message: String?
    ): HighlightInfo.Builder =
        highlightInfo(
            textRange = range,
            severity = HighlightSeverity.ERROR,
            type = HighlightInfoType.ERROR,
            message = message
        )

}


fun HighlightInfo.Builder.withQuickFix(vararg fixes: IntentionAction): HighlightInfo.Builder {
    for (fix in fixes) {
        registerFix(fix, null, null, null, null)
    }
    return this
}

internal operator fun HighlightInfoHolder.plusAssign(highlightInfo: HighlightInfo) {
    add(highlightInfo)
}

internal operator fun HighlightInfoHolder.plusAssign(highlightInfo: HighlightInfo.Builder) {
    add(highlightInfo.createUnconditionally())
}
