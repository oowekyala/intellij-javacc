package com.github.oowekyala.ijcc.ide.intentions

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.JccTokenReferenceRegexUnit
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.lang.psi.safeReplace
import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

/**
 * @author Clément Fournier
 * @since 1.2
 */
class ReplaceSupersedingUsageWithReferenceIntentionFix(private val toReplace: JccLiteralRegexUnit,
                                                       private val tokenName: String) : IntentionAction {
    override fun startInWriteAction(): Boolean = true

    override fun getFamilyName(): String = "Replace superseding string with reference to this token"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

    override fun getText(): String = familyName

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null) throw IllegalArgumentException("This intention requires an editor")


        FileModificationService.getInstance().prepareFileForWrite(toReplace.containingFile)
        val newRegexUnit: JccTokenReferenceRegexUnit = project.jccEltFactory.createRegexElement("<$tokenName>")

        toReplace.safeReplace(newRegexUnit)
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)

    }
}
