package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccRegularExpressionReference
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile


/**
 * @author Clément Fournier
 * @since 1.0
 */
class TokenInliningIntention(psiElement: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(psiElement) {
    override fun getFamilyName(): String = name

    override fun getText(): String = "Inline literal reference"

    override fun isAvailable(project: Project,
                             file: PsiFile,
                             startElement: PsiElement,
                             endElement: PsiElement): Boolean {

        return startElement is JccRegularExpressionReference && startElement.reference.resolveToken()?.asSingleLiteral() != null
    }

    override fun invoke(project: Project,
                        file: PsiFile,
                        editor: Editor?,
                        startElement: PsiElement,
                        endElement: PsiElement) {

        val ref = startElement as? JccRegularExpressionReference ?: return

        val literal = startElement.reference.resolveToken()?.asSingleLiteral()
        if (literal == null) {
            Log { debug("Weird input to the invoke method (asSingleLiteral is null)") }
            return
        }

        ref.replace(JccElementFactory.createLiteralRegex(project, literal.text))
    }

    private object Log : EnclosedLogger()
}