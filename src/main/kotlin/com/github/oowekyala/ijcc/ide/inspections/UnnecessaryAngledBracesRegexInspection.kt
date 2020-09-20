package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.psi.*
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
class UnnecessaryAngledBracesRegexInspection : JccInspectionBase(InspectionName) {

    @Language("HTML")
    override fun getStaticDescription(): String? = """
        Reports unnecessary angled braces around a string literal or a token reference.
        E.g. <b><code>&lt; "foo" &gt;</code></b> is equivalent to <b><code>"foo"</code></b>.
        For token references, e.g. <code>&lt; &lt;FOO&gt; &gt;</code> is only equivalent
        to <code>&lt;FOO&gt;</code> if the regular expression <code>FOO</code> is not private.
    """.trimIndent()


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : JccVisitor() {
            override fun visitContainerRegularExpression(o: JccContainerRegularExpression) {
                if (o.isUnclosed) return

                val regex = o.regexElement ?: return
                if (regex is JccLiteralRegexUnit || regex is JccTokenReferenceRegexUnit && regex.typedReference.resolveToken()?.isPrivate == false) {
                    holder.registerProblem(o, ProblemDescription, MyQuickFix())
                }
            }
        }


    companion object {

        private object Log : EnclosedLogger()

        const val InspectionName = "Unnecessary angled braces around regex"
        const val ProblemDescription = "Unnecessary angled braces"
        const val QuickFixName = "Unwrap angled braces"


        private class MyQuickFix : LocalQuickFix {
            override fun getFamilyName(): String = name

            override fun getName(): String = QuickFixName

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

                try {
                    val inline = descriptor.psiElement as JccContainerRegularExpression
                    val regex = inline.regexElement!!.promoteToRegex()
                    inline.safeReplace(regex)
                } catch (e: IncorrectOperationException) {
                    Log { error(e) }
                }
            }
        }
    }
}
