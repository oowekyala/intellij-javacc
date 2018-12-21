package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexpUnit
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.safeReplace
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
class ReplaceLiteralWithReferenceIntention : PsiElementBaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf { it.node.elementType == JavaccTypes.JCC_STRING_LITERAL }
                ?.let { it.parent as? JccLiteralRegexpUnit }
                ?.let { it.typedReference?.resolve() }
                ?.let { it.name != null && it.asSingleLiteral() != null } ?: false

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val ref = element.parent as JccLiteralRegexpUnit

        val name = ref.typedReference!!.resolve()!!.name!!

        ref.safeReplace(JccElementFactory.createRegexReferenceUnit(project, "<$name>"))
    }

    override fun getFamilyName(): String = text

    override fun getText(): String = "Replace literal with reference"

    private object Log : EnclosedLogger()
}