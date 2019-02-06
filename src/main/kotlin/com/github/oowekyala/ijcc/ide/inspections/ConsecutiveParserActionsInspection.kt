package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.psi.JccParserActionsUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createExpansion
import com.github.oowekyala.ijcc.lang.psi.nextSiblingNoWhitespace
import com.github.oowekyala.ijcc.lang.psi.siblingRangeTo
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.IncorrectOperationException
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ConsecutiveParserActionsInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription() = """
        Reports consecutive parser actions unit, e.g. <code>{foo();} {bar();}</code>,
        which can be rewritten as <code>{foo(); bar();}</code>.
    """.trimIndent()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {
                override fun visitParserActionsUnit(o: JccParserActionsUnit) {

                    var rightEdge: JccParserActionsUnit? = o.nextSiblingNoWhitespace as? JccParserActionsUnit
                    if (rightEdge != null) {


                        var myRange = o.textRange.union(rightEdge.textRange)
                        var last: JccParserActionsUnit = rightEdge

                        rightEdge = rightEdge.nextSiblingNoWhitespace as? JccParserActionsUnit

                        while (rightEdge != null) {
                            last = rightEdge
                            myRange = myRange.union(rightEdge.textRange)
                            rightEdge = rightEdge.nextSiblingNoWhitespace as? JccParserActionsUnit
                        }

                        holder.registerProblem(
                            o.parent,
                            ProblemDescription,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            MyQuickFix(SmartPointerManager.createPointer(o), SmartPointerManager.createPointer(last))
                        )
                    }
                }
            }


    companion object {

        const val DisplayName = "Consecutive parser actions unit"
        const val ProblemDescription = "These parser actions units can be merged"
        const val QuickFixName = "Merge parser actions"

        private object LOG : EnclosedLogger()

        private class MyQuickFix(val firstPointer: SmartPsiElementPointer<JccParserActionsUnit>,
                                 val lastPointer: SmartPsiElementPointer<JccParserActionsUnit>) : LocalQuickFix {
            override fun getFamilyName(): String = name

            override fun getName(): String = QuickFixName

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {


                val first = firstPointer.element ?: return
                val last = lastPointer.element ?: return

                val toMerge = first.siblingRangeTo(last)
                    .filterIsInstance<JccParserActionsUnit>()
                    .toList()
                    .takeIf { it.size > 1 }
                    ?: return


                val newBlock =
                        toMerge.joinToString(separator = " ", prefix = "{", postfix = "}") {
                            it.javaBlock.text.removeSurrounding("{", "}")
                        }.let {
                            createExpansion(project, it) as JccParserActionsUnit
                        }

                try {
                    first.replace(newBlock)
                    if (toMerge.size == 2) {
                        last.delete()
                    } else {
                        toMerge[1].parent.deleteChildRange(toMerge[1], toMerge.last())
                    }
                } catch (e: IncorrectOperationException) {
                    LOG { error(e) }
                }
            }
        }
    }
}