package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException

/**
 * @author Clément Fournier
 * @since 1.0
 */
class ConsecutiveParserActionsInspection : JavaccInspectionBase(DisplayName) {

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

                        holder.manager.createProblemDescriptor(
                            o.parent,
                            myRange.relativize(o.parent.textRange),
                            ProblemDescription,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOnTheFly,
                            MyQuickFix
                        ).let {
                            MindfulProblemDescriptor(it)
                        }.also {
                            it.putData(fstActionKey, o)
                            it.putData(lstActionKey, last)
                            holder.registerProblem(it.descriptor)
                        }
                    }
                }
            }


    companion object {

        private val fstActionKey = ProblemDataKey<JccParserActionsUnit>("fst")
        private val lstActionKey = ProblemDataKey<JccParserActionsUnit>("lst")

        const val DisplayName = "Consecutive parser actions unit"
        const val ProblemDescription = "These parser actions units can be merged"
        const val QuickFixName = "Merge parser actions"

        private object LOG : EnclosedLogger()

        private object MyQuickFix : LocalQuickFix {
            override fun getFamilyName(): String = name

            override fun getName(): String = QuickFixName

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {

                val (first, last) = with(MindfulProblemDescriptor(descriptor)) {
                    Pair(
                        getData(fstActionKey)!!,
                        getData(lstActionKey)!!
                    )
                }

                val toMerge = first.siblingRangeTo(last)
                    .filterMapAs<JccParserActionsUnit>()
                    .toList()
                    .takeIf { it.size > 1 }
                    ?: return


                val newBlock =
                        toMerge.joinToString(separator = " ", prefix = "{", postfix = "}") {
                            it.javaBlock.text.removeSurrounding("{", "}")
                        }.let {
                            JccElementFactory.createBnfExpansion(project, it) as JccParserActionsUnit
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