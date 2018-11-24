package com.github.oowekyala.ijcc.inspections

import com.github.oowekyala.ijcc.lang.psi.JccInlineRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryInlineRegexInspection : JavaccInspectionBase(InspectionName) {

    // unless the string is
    // * in a token spec
    // * or in a token spec

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JccVisitor() {

            override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
                val regex = o.regularExpression
                if (regex is JccLiteralRegularExpression) {
                    holder.registerProblem(o, ProblemDescription)
                    holder.registerProblem(o, ProblemDescription, MyQuickFix())
                }
            }
        }
    }


    companion object : BaseInspectionCompanion() {

        const val InspectionName = "Unnecessary angled braces around literal regex"
        const val ProblemDescription = "This inline regex could be replaced by a literal regex"
        const val QuickFixName = "Unwrap string literal"

        object Provider : InspectionToolProvider {
            override fun getInspectionClasses(): Array<Class<out Any>> =
                    arrayOf(UnnecessaryInlineRegexInspection::class.java)
        }

        private class MyQuickFix : LocalQuickFix {
            override fun getFamilyName(): String = name

            override fun getName(): String = QuickFixName

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

                try {
                    val inline = descriptor.psiElement as JccInlineRegularExpression
                    val regex = inline.regularExpression!!
                    inline.replace(regex)
                } catch (e: IncorrectOperationException) {
                    LOG.error(e)
                }
            }
        }
    }
}