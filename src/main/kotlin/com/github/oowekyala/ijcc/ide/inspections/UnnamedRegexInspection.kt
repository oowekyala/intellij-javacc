package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.intentions.AddNameToRegexIntention
import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInspection.IntentionWrapper
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnamedRegexInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription(): String = """
        Reports unnamed tokens and regex specs. Naming tokens <b>improves
        error messages</b>, and unnamed tokens may cause JavaCC warnings
        and unexpected behaviour. Additionally, JavaCC ignores regex specs
        that just reference another token.
    """.trimIndent()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : JccVisitor() {
        override fun visitContainerRegularExpression(o: JccContainerRegularExpression) {
            if (o.parent !is JccRegexSpec && o.regexElement !is JccTokenReferenceRegexUnit && o.regexElement !is JccLiteralRegexUnit) {
                holder.registerProblem(o, GenericProblemDesc)
            }
        }

        override fun visitRegexSpec(o: JccRegexSpec) {
            if (o.regexKind == RegexKind.TOKEN) {
                val regex = o.regularExpression

                if (regex !is JccNamedRegularExpression) {
                    val desc = when (regex) {
                        is JccRefRegularExpression -> FreeStandingReferenceProblemDesc
                        else                       -> GenericProblemDesc
                    }
                    holder.registerProblem(
                        o,
                        desc,
                        IntentionWrapper.wrapToQuickFix(AddNameToRegexIntention(), o.containingFile)
                    )
                }
            }
        }
    }

    companion object {
        const val DisplayName = "Unnamed regular expression"
        const val GenericProblemDesc = "Unnamed token"
        const val FreeStandingReferenceProblemDesc =
            "JavaCC ignores free-standing regular expression references unless they have a different name"
    }
}