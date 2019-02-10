package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInspection.IntentionWrapper
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Simply deletes an expansion.
 *
 * @author Clément Fournier
 * @since 1.1
 */
class DeleteExpansionIntention(val name: String = "Delete expansion")
    : SelfTargetingOffsetIndependentIntention<JccExpansion>(JccExpansion::class.java, name), LowPriorityAction {

    override fun applyTo(project: Project, editor: Editor?, element: JccExpansion) = element.delete()

    override fun isApplicableTo(element: JccExpansion): Boolean = element.parent !is JccBnfProduction

    companion object {
        fun quickFix(name: String = "Delete expansion", file: PsiFile): LocalQuickFix =
            IntentionWrapper.wrapToQuickFix(DeleteExpansionIntention(name), file)
    }
}

