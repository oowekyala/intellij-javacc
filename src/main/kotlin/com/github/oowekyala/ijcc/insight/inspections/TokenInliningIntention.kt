package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccTokenReferenceUnit
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.typedReference
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement


/**
 * @author Clément Fournier
 * @since 1.0
 */
class TokenInliningIntention : PsiElementBaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf { it.node.elementType == JavaccTypes.JCC_IDENT }
                ?.let { it.parent as? JccIdentifier }
                ?.let { it.parent as? JccTokenReferenceUnit }
                ?.let { it.typedReference.resolveToken()?.asSingleLiteral() } != null

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val ref = element.parent.parent as? JccTokenReferenceUnit ?: return

        val literal = ref.typedReference.resolveToken()?.asSingleLiteral()
        if (literal == null) {
            Log { debug("Weird input to the invoke method (asSingleLiteral is null)") }
            return
        }

        ref.replace(JccElementFactory.createLiteralRegex(project, literal.text))
    }

    override fun getFamilyName(): String = text

    override fun getText(): String = "Inline literal reference"


    private object Log : EnclosedLogger()
}