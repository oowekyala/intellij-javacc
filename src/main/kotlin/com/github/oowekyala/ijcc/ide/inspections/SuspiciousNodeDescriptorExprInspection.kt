package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.util.EnclosedLogger
import com.github.oowekyala.ijcc.util.insert
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.IncorrectOperationException
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class SuspiciousNodeDescriptorExprInspection : JccInspectionBase(InspectionName) {

    @Language("HTML")
    override fun getStaticDescription(): String? = """
            This inspection tries to detect parenthesized expansions that are parsed by
            JJTree as the condition on a node descriptor. This arises because of syntactic
            ambiguity between the two. For example:
            <code>
                ( ... ) #N ( a() )
            </code>
            is ambiguous; you have to use the explicit condition:
            <code>
                ( ... ) #N<b>(true)</b> ( a() )
            </code>

            This inspection uses a natural code style convention to guess whether this is
            intentional or not. A node descriptor expression is flagged iff its opening
            parenthesis does not immediately follow the identifier, with no whitespace in-between.
            For example the following will be reported:
            <code>
                ( ... ) #Name
                ( a() )
            </code>
            since there's whitespace between <code>'#Name'</code>. The following will not be reported:
            <code>
                ( ... ) #Name( a() )
            </code>
    """.trimIndent()

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {

                override fun visitJjtreeNodeDescriptor(o: JccJjtreeNodeDescriptor) {
                    if (o.expansionUnit == null || o.descriptorExpr == null) return

                    val identifier = o.nameIdentifier ?: return

                    if (identifier.nextSibling?.node?.elementType == JccTypes.JCC_JJTREE_NODE_DESCRIPTOR_EXPR) return
                    else {
                        holder.registerProblem(
                            o.descriptorExpr!!,
                            makeDescription(o),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            MyAddTrueQuickFix
                        )
                    }
                }
            }

    companion object {


        const val InspectionName = "Suspicious JJTree node descriptor expression"

        fun makeDescription(nodeDescriptor: JccJjtreeNodeDescriptor) =
                "Ambiguous node descriptor expression for #${nodeDescriptor.name}"

        private object LOG : EnclosedLogger()
        private object MyAddTrueQuickFix : LocalQuickFix {
            override fun getFamilyName(): String = "Add '(true)' to the node descriptor"

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                try {
                    val parens = descriptor.psiElement as JccJjtreeNodeDescriptorExpr
                    val nodeDescriptor = parens.parent as JccJjtreeNodeDescriptor
                    val scopedUnit = nodeDescriptor.parent as JccScopedExpansionUnit
                    val context = scopedUnit.parent

                    val ident = scopedUnit.jjtreeNodeDescriptor.namingLeaf

                    val identEndOffset =
                            nodeDescriptor.startOffsetInParent + ident.startOffsetInParent + ident.textLength
                    val newText = scopedUnit.text.insert(identEndOffset, "(true)")

                    val newExpansion =
                            JccElementFactory.createBnfExpansion(parens.project, newText) as JccExpansionSequence

                    // insert into existing sequence
                    if (context is JccExpansionSequence) {
                        val newExpansionUnits = newExpansion.expansionUnitList
                        context.addRangeAfter(newExpansionUnits.first(), newExpansionUnits.last(), scopedUnit)
                        scopedUnit.delete()
                    } else {
                        scopedUnit.replace(newExpansion)
                    }
                } catch (e: IncorrectOperationException) {
                    LOG { error(e) }
                }
            }
        }
    }
}