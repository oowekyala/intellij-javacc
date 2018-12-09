package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccParenthesizedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException
import com.siyeh.InspectionGadgetsBundle
import org.intellij.lang.annotations.Language
import javax.swing.JComponent

/**
 * @author Clément Fournier
 * @since 1.0
 */
class UnnecessaryParenthesesInspection : JavaccInspectionBase(InspectionName) {
    var keepAroundAssignment: Boolean = true
    var keepAroundLookahead: Boolean = true
    var keepBeforeParserActions: Boolean = false

    @Language("HTML")
    override fun getStaticDescription(): String? = """
            This inspection reports parentheses when they are redundant.
            E.g. in BNF expansions:
            <code>
                ("foo" "bar") | "bzaz"           // unnecessary
                ("foo" | "bar") "bzaz"           // necessary
            </code>
            The checkboxes below allow to whitelist some constructs for which
            parentheses may improve readability.
    """.trimIndent()

    override fun isEnabledByDefault(): Boolean = true

    // UnnecessaryParentheses as an ID is already taken
    override fun getID(): String = "JavaCCUnnecessaryParentheses"
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
        keepBeforeParserActions = keepBeforeParserActions
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {
                private val config = getConfig()
                override fun visitParenthesizedExpansionUnit(o: JccParenthesizedExpansionUnit) {
                    if (o.isUnnecessary(config)) {
                        holder.registerProblem(
                            o,
                            ProblemDescription,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            MyQuickFix
                        )
                    }
                }
            }

    companion object {


        val InspectionName = InspectionGadgetsBundle.message("unnecessary.parentheses.display.name")
        val ProblemDescription = InspectionName
        val QuickFixName = InspectionGadgetsBundle.message("unnecessary.parentheses.remove.quickfix")


        private object LOG : EnclosedLogger()
        private object MyQuickFix : LocalQuickFix {
            override fun getFamilyName(): String = QuickFixName

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                try {
                    val parens = descriptor.psiElement as JccParenthesizedExpansionUnit
                    val inside = parens.expansion!!
                    parens.replace(inside)
                } catch (e: IncorrectOperationException) {
                    LOG { error(e) }
                }
            }
        }
    }
}