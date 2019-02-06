package com.github.oowekyala.ijcc.ide.intentions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

abstract class JccEditorIntentionBase(name: String) : JccIntentionBase(name) {

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) throw IllegalArgumentException("This intention requires an editor")


        val runnable = Runnable {
            run(project, editor, element)
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
        }

        if (startInWriteAction()) {
            runnable.run()
        } else {
            ApplicationManager.getApplication().runWriteAction(runnable)
        }
    }


    protected abstract fun run(project: Project, editor: Editor, element: PsiElement): ()->Unit

}