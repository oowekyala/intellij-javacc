package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccInlineRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexpUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryInlineRegexInspection : JavaccInspectionBase(InspectionName) {

    @Language("HTML")
    override fun getStaticDescription(): String? = """
        Reports unnecessary angled braces around a string literal.
        E.g. <b><code>&lt; "foo" &gt;</code></b> is equivalent to <b><code>"foo"</code></b>.
    """.trimIndent()


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {
                override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
                    val regex = o.regexpElement
                    if (regex is JccLiteralRegexpUnit) {
                        holder.registerProblem(o, ProblemDescription, MyQuickFix())
                    }
                }
            }


    companion object {

        private object Log : EnclosedLogger()

        const val InspectionName = "Unnecessary angled braces around literal regex"
        const val ProblemDescription = "Unnecessary angled braces"
        const val QuickFixName = "Unwrap string literal"

        private class MyQuickFix : LocalQuickFix {
            override fun getFamilyName(): String = name

            override fun getName(): String = QuickFixName

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

                try {
                    val inline = descriptor.psiElement as JccInlineRegularExpression
                    val regex = inline.regexpElement!!
                    inline.replace(regex)
                } catch (e: IncorrectOperationException) {
                    Log { error(e) }
                }
            }
        }
    }
}