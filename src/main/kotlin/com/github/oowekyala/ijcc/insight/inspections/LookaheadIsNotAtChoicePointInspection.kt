package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class LookaheadIsNotAtChoicePointInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription(): String = """
        Reports lookahead specifications that are not at choice points. Such specifications
        are ignored by JavaCC and will produce a warning.

        <p>
            There are 4 different kinds of choice points in JavaCC:
            <ul>
                <li><code>( exp1 | exp2 | ... )</code></li>
                <li><code>( exp )?</code> or <code>[ exp ]</code></li>
                <li><code>( exp )*</code></li>
                <li><code>( exp )+</code></li>
            </ul>

            For each of these, the lookahead specification must be the first item
            in the sequence. E.g. <code>(LOOKAHEAD(2) "f" "g")?</code> is ok, but
            <code>("f" LOOKAHEAD(2) "g")?</code> is not, because the lookahead spec
            is in the middle of the sequence.

            More information about the choice calculation and what a choicepoint is is available
            on the official <a href="https://javacc.org/tutorials/lookahead">Lookahead Tutorial</a>.
        </p>

        <p>
            For nested lookahead specifications (e.g. <code>LOOKAHEAD(1, LOOKAHEAD(2, a()) a() | Foo()) Foo()</code>),
            the syntactic lookahead is not evaluated. Only the semantic lookahead is.
        </p>
    """.trimIndent()

    override fun isEnabledByDefault(): Boolean = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {

                override fun visitLocalLookahead(la: JccLocalLookahead) {

                    if (la.lexicalAmount != null && la.lexicalAmount != 0) {

                        val lookfrom = la.parent as? JccExpansionSequence ?: la

                        val isGParentChoicePoint = when (val gParent = lookfrom.parent) {
                            is JccExpansionAlternative       -> true
                            is JccOptionalExpansionUnit      -> true
                            is JccParenthesizedExpansionUnit -> gParent.occurrenceIndicator != null
                            else                             -> false
                        }

                        val parent = la.parent
                        val isFirstInSeq = parent is JccExpansionSequence && parent.expansionUnitList[0] === la

                        if (!isGParentChoicePoint || !isFirstInSeq) {

                            val desc = if (la.isSemantic) SemanticProblemDesc else IgnoredProblemDesc

                            holder.registerProblem(
                                la,
                                desc,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                            )
                        } else if (la.ancestors(includeSelf = false).any { it is JccLocalLookahead } && la.isSyntactic) {
                            // don't report both

                            holder.registerProblem(
                                la,
                                NestedProblemDesc,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                            )
                        }
                    }
                }
            }


    companion object {

        private const val DisplayName = "LOOKAHEAD is not at a choice point"
        const val IgnoredProblemDesc = "LOOKAHEAD is not at a choice point, it will be ignored"
        const val NestedProblemDesc =
                "Only semantic lookahead is considered when it is nested.  Syntactic lookahead is ignored."
        const val SemanticProblemDesc =
                "LOOKAHEAD is not at a choice point, only semantic lookahead will be performed"

    }
}
