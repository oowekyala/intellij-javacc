package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.ide.intentions.DeleteExpansionIntention
import com.github.oowekyala.ijcc.lang.cfa.isNextStep
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.deleteWhitespace
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class EmptyParserActionsInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription() = """
        Reports empty parser actions unit, which are unnecessary and
        may cause some JavaCC warnings.
    """.trimIndent()


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : JccVisitor() {
            override fun visitParserActionsUnit(o: JccParserActionsUnit) {

                if (o.text.deleteWhitespace() == "{}") {

                    val parentScope =
                        o.ancestors(includeSelf = false)
                            .filterIsInstance<JjtNodeClassOwner>()
                            .firstOrNull { it.isNotVoid }

                    if (parentScope != null && o.isNextStep(parentScope) && o.prevSiblingNoWhitespace is JccParserActionsUnit)
                        return

                    holder.registerProblem(
                        o,
                        ProblemDescription,
                        DeleteExpansionIntention.quickFix(FixDescription, o.containingFile)
                    )
                }
            }
        }


    companion object {
        const val DisplayName = "Empty parser actions unit"
        const val ProblemDescription = "Empty parser actions unit"
        const val FixDescription = "Delete expansion unit"
    }
}
