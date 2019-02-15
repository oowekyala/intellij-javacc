package com.github.oowekyala.ijcc.ide.highlight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccHighlightUtil {

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

    fun wrongReferenceInfo(element: PsiElement,
                           message: String): HighlightInfo =
        wrongReferenceInfo(element.textRange, message)

    fun wrongReferenceInfo(range: TextRange,
                           message: String): HighlightInfo =
        highlightInfo(
            textRange = range,
            severity = HighlightSeverity.ERROR,
            type = HighlightInfoType.WRONG_REF,
            message = message
        )

    fun warningInfo(element: PsiElement,
                    message: String): HighlightInfo =
        highlightInfo(
            textRange = element.textRange,
            severity = HighlightSeverity.WARNING,
            type = HighlightInfoType.WARNING,
            message = message
        )

    fun errorInfo(element: PsiElement,
                  message: String?): HighlightInfo =
        errorInfo(element.textRange, message)

    fun errorInfo(range: TextRange,
                  message: String?): HighlightInfo =
        highlightInfo(
            textRange = range,
            severity = HighlightSeverity.ERROR,
            type = HighlightInfoType.ERROR,
            message = message
        )

}

fun HighlightInfo.withQuickFix(range: TextRange = TextRange(startOffset, endOffset), vararg fixes: IntentionAction) =
    this.also {
        QuickFixAction.registerQuickFixActions(this, range, fixes.toList())
    }


fun HighlightInfo.withQuickFix(vararg fixes: IntentionAction) =
    this.also {
        fixes.forEach {
            QuickFixAction.registerQuickFixAction(this, it)
        }
    }

internal operator fun HighlightInfoHolder.plusAssign(highlightInfo: HighlightInfo) {
    add(highlightInfo)

}