package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.model.RegexKind
import com.github.oowekyala.ijcc.ide.intentions.AddNameToRegexIntention
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
        error messages</b>. Additionally, JavaCC ignores regex specs that
        just reference another token.
    """.trimIndent()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : JccVisitor() {
        override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
            if (o.parent !is JccRegexprSpec && o.regexpElement !is JccTokenReferenceUnit && o.regexpElement !is JccLiteralRegexpUnit) {
                holder.registerProblem(o, GenericProblemDesc)
            }
        }

        override fun visitRegexprSpec(o: JccRegexprSpec) {
            if (o.regexKind == RegexKind.TOKEN) {
                val regex = o.regularExpression

                if (regex !is JccNamedRegularExpression) {
                    val desc = when (regex) {
                        is JccRegularExpressionReference -> FreeStandingReferenceProblemDesc
                        else                             -> GenericProblemDesc
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