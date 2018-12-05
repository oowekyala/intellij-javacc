package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccParenthesizedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryParenthesesInspection : JavaccInspectionBase("Unnecessary parentheses") {

    // TODO description
    override fun getStaticDescription(): String? = """foo"""

    override fun isEnabledByDefault(): Boolean = true

    // UnnecessaryParentheses as an ID is already taken
    override fun getID(): String = "JavaCCUnnecessaryParentheses"
    // TODO quickfix
    // TODO parenthesized regex unit

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {
                override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {
                    if (!o.isNecessary()) {
                        holder.registerProblem(o, "Unnecessary parentheses", ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                    }
                }
            }
}