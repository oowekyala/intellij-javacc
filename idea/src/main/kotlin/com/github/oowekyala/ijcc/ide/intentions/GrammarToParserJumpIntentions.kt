package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.model.parserQualifiedName
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.getJavaClassFromQname
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Jump from grammar file to parser file.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class GrammarToParserJumpIntention : JccIntentionBase("Jump to parser file") {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {


        val file = element.containingFile as? JccFile ?: return false

        val qname = file.grammarOptions.parserQualifiedName

        return getJavaClassFromQname(file, qname) != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {

        val file = element.containingFile as? JccFile ?: return

        val qname = file.grammarOptions.parserQualifiedName
        val targetPsiClass = getJavaClassFromQname(file, qname)!!
        val targetVirtualFile = targetPsiClass.containingFile.virtualFile


        val jumpOffset: Int = element.ancestorOrSelf<JccNonTerminalProduction>()?.let {
            val methods = targetPsiClass.findMethodsByName(it.name, /*checkBases*/false)
            if (methods.size == 1) methods.first()
            else null
        }?.textOffset ?: -1

        ApplicationManager.getApplication().invokeLater {
            val edtManager = FileEditorManager.getInstance(project)

            edtManager.openFile(targetVirtualFile, true)

            if (jumpOffset >= 0) {
                edtManager.selectedTextEditor?.caretModel?.currentCaret?.moveToOffset(jumpOffset)
            }
        }
    }
}
