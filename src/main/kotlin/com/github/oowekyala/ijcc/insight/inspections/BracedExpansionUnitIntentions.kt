package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.JccOptionalExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccParenthesizedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccZeroOrOne
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.psi.safeReplace
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * @author Clément Fournier
 * @since 1.0
 */

class ReplaceParenthesizedOptionalWithBracedIntention : PsiElementBaseIntentionAction() {

    private fun getParenthesizedParent(psiElement: PsiElement): JccParenthesizedExpansionUnit? = psiElement.let {
        when (it.node.elementType) {
            JccTypes.JCC_RPARENTH, JccTypes.JCC_LPARENTH ->
                it.parent as? JccParenthesizedExpansionUnit
            JccTypes.JCC_QUESTION                                                       ->
                it.let { it.parent as? JccZeroOrOne }?.let { it.parent as? JccParenthesizedExpansionUnit }
            else                                                                                                       -> null
        }
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            getParenthesizedParent(element)?.let { it.occurrenceIndicator is JccZeroOrOne } == true

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val ref = getParenthesizedParent(element)!!
        val expansionText = ref.expansion?.text ?: ""
        ref.safeReplace(JccElementFactory.createBracedExpansionUnit(project, "[$expansionText]"))
    }

    override fun getFamilyName(): String = text

    override fun getText(): String = "Replace parentheses with braces"

    private object Log : EnclosedLogger()
}


class ReplaceBracedExpansionUnitWithParenthesizedIntention : PsiElementBaseIntentionAction() {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
            element.takeIf {
                when (it.node.elementType) {
                    JccTypes.JCC_RBRACKET, JccTypes.JCC_LBRACKET -> true
                    else                                                                                                       -> false
                }
            }
                ?.let { it.parent is JccOptionalExpansionUnit } == true

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val ref = element.parent as JccOptionalExpansionUnit
        val expansionText = ref.expansion?.text ?: ""
        ref.safeReplace(JccElementFactory.createParenthesizedExpansionUnit(project, "($expansionText)?"))
    }

    override fun getFamilyName(): String = text

    override fun getText(): String = "Replace braces with parentheses"

    private object Log : EnclosedLogger()
}