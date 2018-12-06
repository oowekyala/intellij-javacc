package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccParenthesizedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import com.intellij.psi.PsiElementVisitor
import javax.swing.JComponent

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryParenthesesInspection : JavaccInspectionBase("Unnecessary parentheses") {
    var keepAroundAssignment: Boolean = false
    var keepAroundLookahead: Boolean = true
    var keepBeforeParserActions: Boolean = false

    // TODO description
    override fun getStaticDescription(): String? = """foo"""

    override fun isEnabledByDefault(): Boolean = true

    // UnnecessaryParentheses as an ID is already taken
    override fun getID(): String = "JavaCCUnnecessaryParentheses"
    // TODO quickfix
    // TODO parenthesized regex unit

    override fun createOptionsPanel(): JComponent {
        val optionsPanel = MultipleCheckboxOptionsPanel(this)
        optionsPanel.addCheckbox("Keep around assignment", "keepAroundAssignment")
        optionsPanel.addCheckbox("Keep around lookahead", "keepAroundLookahead")
        optionsPanel.addCheckbox("Keep before parser actions", "keepBeforeParserActions")
        return optionsPanel
    }

    private fun getConfig() = ParenthesesConfig(
        keepAroundAssignment = keepAroundAssignment,
        keepAroundLookahead = keepAroundLookahead,
        keepBeforeParserActions = keepBeforeParserActions,
        keepUndocumented = true
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {
                private val config = getConfig()
                override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {
                    if (!o.isNecessary(config)) {
                        holder.registerProblem(o, "Unnecessary parentheses", ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                    }
                }
            }
}