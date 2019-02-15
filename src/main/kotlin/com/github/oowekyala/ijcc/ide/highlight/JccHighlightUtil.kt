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
                      message: String? = null,
                      vararg quickFixes: IntentionAction): HighlightInfo =
        highlightInfo(element.textRange, type, severity, message, *quickFixes)

    fun highlightInfo(textRange: TextRange,
                      type: HighlightInfoType,
                      severity: HighlightSeverity = type.getSeverity(null), // hack
                      message: String? = null,
                      vararg quickFixes: IntentionAction): HighlightInfo =

        HighlightInfo.newHighlightInfo(type)
            .range(textRange)
            .severity(severity)
            .also { if (message != null) it.descriptionAndTooltip(message) }
            .createUnconditionally()
            .also { info ->
                quickFixes.forEach { fix -> QuickFixAction.registerQuickFixAction(info, fix) }
            }

    fun wrongReferenceInfo(element: PsiElement, message: String, vararg quickFixes: IntentionAction): HighlightInfo =
        wrongReferenceInfo(element.textRange, message, *quickFixes)

    fun wrongReferenceInfo(range: TextRange, message: String, vararg quickFixes: IntentionAction): HighlightInfo =
        highlightInfo(
            textRange = range,
            severity = HighlightSeverity.ERROR,
            type = HighlightInfoType.WRONG_REF,
            message = message,
            quickFixes = *quickFixes
        )

    fun warningInfo(element: PsiElement, message: String, vararg quickFixes: IntentionAction): HighlightInfo =
        highlightInfo(
            textRange = element.textRange,
            severity = HighlightSeverity.WARNING,
            type = HighlightInfoType.WARNING,
            message = message,
            quickFixes = *quickFixes
        )

    fun errorInfo(element: PsiElement, message: String?, vararg quickFixes: IntentionAction): HighlightInfo =
        errorInfo(element.textRange, message, *quickFixes)

    fun errorInfo(range: TextRange, message: String?, vararg quickFixes: IntentionAction): HighlightInfo =
        highlightInfo(
            textRange = range,
            severity = HighlightSeverity.ERROR,
            type = HighlightInfoType.ERROR,
            message = message,
            quickFixes = *quickFixes
        )

}

internal operator fun HighlightInfoHolder.plusAssign(highlightInfo: HighlightInfo) {
    add(highlightInfo)

}