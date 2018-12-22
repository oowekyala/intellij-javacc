package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccParserActionsUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.util.deleteWhitespace
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class EmptyParserActionsInspection : JavaccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription() = """
        Reports empty parser actions unit, which are unnecessary and
        may cause some JavaCC errors.
    """.trimIndent()


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {
                override fun visitParserActionsUnit(o: JccParserActionsUnit) {

                    if (o.text.deleteWhitespace() == "{}") {
                        holder.registerProblem(
                            o,
                            ProblemDescription
                        )
                    }

                }

            }


    companion object {
        const val DisplayName = "Empty parser actions unit"
        const val ProblemDescription = "Empty parser actions unit"

        // TODO quickfix
    }
}