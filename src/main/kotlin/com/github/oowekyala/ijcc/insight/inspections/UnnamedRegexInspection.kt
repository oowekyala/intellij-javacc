package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccInlineRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.model.RegexKind
import com.github.oowekyala.ijcc.util.LoggerCompanion
import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnamedRegexInspection : JavaccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription(): String = """
        Reports unnamed tokens and regex specs. Naming tokens <b>improves
        error messages</b>. This only reports token specs of the TOKEN kind,
        and regular expressions found in BNF expansions.
    """.trimIndent()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : JccVisitor() {
        override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
            if (o.parent !is JccRegexprSpec) {
                holder.registerProblem(o, "Unnamed token")
            }
        }

        override fun visitRegexprSpec(o: JccRegexprSpec) {
            if (o.regexKind == RegexKind.TOKEN) {
                val regex = o.regularExpression
                if (regex !is JccNamedRegularExpression) {
                    holder.registerProblem(o, "Unnamed token")
                }
            }
        }
    }

    companion object : LoggerCompanion {
        const val DisplayName = "Unnamed regular expression"
    }
}