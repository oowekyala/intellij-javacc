package com.github.oowekyala.ijcc.insight.intentions

import com.github.oowekyala.ijcc.lang.psi.JccInlineRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegex
import com.github.oowekyala.ijcc.lang.psi.safeReplace
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageBaseFix
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInsight.template.TemplateBuilderImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

/** Wraps a regex into a named regex context. */
class AddNameToRegexIntention :
    SelfTargetingOffsetIndependentIntention<JccRegularExpression>(
        JccRegularExpression::class.java,
        "Add name to regular expression"
    ), LowPriorityAction {


    override fun applyTo(project: Project, editor: Editor?, element: JccRegularExpression) {

        if (editor == null) throw IllegalArgumentException("This intention requires an editor")

        val regexText = when (element) {
            is JccInlineRegularExpression -> element.regexpElement?.text
            else                          -> element.text
        } ?: ""


        val newExpr = createRegex<JccNamedRegularExpression>(element.project, "< FOO: $regexText >")
        val runnable = Runnable {
            val replaced = element.safeReplace(newExpr)
            startTemplate(
                project,
                editor,
                replaced
            )
        }

        if (startInWriteAction()) {
            runnable.run()
        } else {
            ApplicationManager.getApplication().runWriteAction(runnable)
        }
    }

    override fun isApplicableTo(element: JccRegularExpression): Boolean = element !is JccNamedRegularExpression


    companion object {
        private fun startTemplate(project: Project,
                                  editor: Editor,
                                  namedRegularExpression: JccNamedRegularExpression) {
            val builder = TemplateBuilderImpl(namedRegularExpression)
            val rangeMarker = editor.document.createRangeMarker(namedRegularExpression.textRange)

            builder.replaceElement(namedRegularExpression.nameIdentifier, TextExpression(""))
            val template = builder.buildTemplate()
            editor.caretModel.moveToOffset(rangeMarker.startOffset)

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
            editor.document.deleteString(rangeMarker.startOffset, rangeMarker.endOffset)

            rangeMarker.dispose()

            CreateFromUsageBaseFix.startTemplate(editor, template, project)
        }
    }

}