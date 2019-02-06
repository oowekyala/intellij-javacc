package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.intentions.DeleteExpansionIntention
import com.github.oowekyala.ijcc.lang.psi.JccLocalLookahead
import com.github.oowekyala.ijcc.lang.psi.JccParserActionsUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.psi.ancestors
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ActionWithinLookaheadInspection : JccInspectionBase(DisplayName) {


    @Language("HTML")
    override fun getStaticDescription() = """
        Reports parser actions declared inside local lookahead specifications.
        These are ignored during lookahead evaluation.
    """.trimIndent()


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {
                override fun visitParserActionsUnit(o: JccParserActionsUnit) {

                    if (o.ancestors(false).any { it is JccLocalLookahead }) {
                        holder.registerProblem(
                            o,
                            ProblemDescription,
                            DeleteExpansionIntention.quickFix(
                                FixDescription, o.containingFile
                            )
                        )
                    }
                }
            }


    companion object {
        const val DisplayName = "Parser actions within lookahead specifications are ignored"
        const val ProblemDescription = DisplayName
        const val FixDescription = "Delete expansion unit"
    }
}