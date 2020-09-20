package com.github.oowekyala.ijcc.ide.intentions

import com.intellij.codeInsight.FileModificationService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * A [SelfTargetingOffsetIndependentIntention] that needs an editor to run.
 */
abstract class JccSelfTargetingEditorIntentionBase<T : PsiElement>(
    target: Class<T>,
    name: String
) : SelfTargetingOffsetIndependentIntention<T>(target, name) {


    final override fun applyTo(project: Project, editor: Editor?, element: T) {
        if (editor == null) throw IllegalArgumentException("This intention requires an editor")

        val kRunnable = run(project, editor, element)

        FileModificationService.getInstance().prepareFileForWrite(element.containingFile)
        if (startInWriteAction()) {
            kRunnable()
        } else {
            ApplicationManager.getApplication().runWriteAction(kRunnable)
        }
    }


    protected abstract fun run(project: Project, editor: Editor, element: T): () -> Unit

}