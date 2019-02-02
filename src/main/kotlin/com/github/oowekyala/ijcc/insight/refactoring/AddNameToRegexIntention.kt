package com.github.oowekyala.ijcc.insight.refactoring

import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegularExpression
import com.github.oowekyala.ijcc.lang.psi.safeReplace
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager

class AddNameToRegexIntention :
    SelfTargetingOffsetIndependentIntention<JccRegularExpression>(
        JccRegularExpression::class.java,
        "Add name to regular expression"
    ), LowPriorityAction {

    override fun isApplicableTo(element: JccRegularExpression) = element !is JccNamedRegularExpression

    override fun applyTo(element: JccRegularExpression, editor: Editor?) {
        if (editor == null) throw IllegalArgumentException("This intention requires an editor")

        val newExpr = createRegularExpression<JccNamedRegularExpression>(element.project, "<FOO: ${element.text}>")

        element.safeReplace(newExpr)

        PsiDocumentManager.getInstance(element.project).doPostponedOperationsAndUnblockDocument(editor.document)
        val dataContext = DataManager.getInstance().getDataContext(editor.component)
//        editor.caretModel.moveToOffset(newExpr.nameIdentifier.textOffset)
        JccInPlaceRenameHandler().doRename(newExpr, editor, dataContext)
    }
}