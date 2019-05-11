package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.ProblemDescriptorImpl
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

    // TODO ignore when the last one is at the end of a node scope!

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : JccVisitor() {
            override fun visitParserActionsUnit(o: JccParserActionsUnit) {

                if (o.prevSiblingNoWhitespace is JccParserActionsUnit) return // the leftmost one will bear the warning

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
                        ProblemDescriptorImpl(
                            o,
                            last,
                            ProblemDescription,
                            arrayOf(
                                MyQuickFix(
                                    SmartPointerManager.createPointer(o),
                                    SmartPointerManager.createPointer(last)
                                )
                            ),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            false,
                            null,
                            isOnTheFly
                        )
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
                        project.jccEltFactory.createExpansion<JccParserActionsUnit>(it)
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
