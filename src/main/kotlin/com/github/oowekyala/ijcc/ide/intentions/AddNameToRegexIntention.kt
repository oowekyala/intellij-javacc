package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.JccContainerRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.lang.psi.safeReplace
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageBaseFix
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInsight.template.TemplateBuilderImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

/** Wraps a regex into a named regex context. */
class AddNameToRegexIntention :
    JccSelfTargetingEditorIntentionBase<JccRegularExpression>(
        JccRegularExpression::class.java,
        "Add name to regex (may change semantics)"
    ), LowPriorityAction {

    override fun run(project: Project, editor: Editor, element: JccRegularExpression): () -> Unit {
        val regexText = when (element) {
            is JccContainerRegularExpression -> element.regexElement?.text
            else                             -> element.text
        } ?: ""


        val newExpr = element.project.jccEltFactory.createRegex<JccNamedRegularExpression>("< FOO: $regexText >")
        return {
            val replaced = element.safeReplace(newExpr)
            startTemplate(project, editor, replaced)
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
