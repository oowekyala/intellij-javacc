package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.1
 */
class RegexMayMatchEmptyStringInspection : JccInspectionBase(DisplayName) {


    @Language("HTML")
    override fun getStaticDescription(): String = """
        Reports token definitions that can match the empty string.
        These can cause infinite loops of empty string matches at
        runtime.
    """.trimIndent()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {


                override fun visitRegexSpec(o: JccRegexSpec) = visitRegularExpressionOwner(o)

                override fun visitRegexExpansionUnit(o: JccRegexExpansionUnit) = visitRegularExpressionOwner(o)

                // this wouldn't be called without explicit override and delegation because
                // other hierarchies take priority in the visitor (regex expansion unit delegates to
                // expansion unit by default)
                override fun visitRegularExpressionOwner(o: JccRegularExpressionOwner) {

                    val r = o.regularExpression

                    val root = r.getRootRegexElement(followReferences = false, unwrapParens = false)

                    if (root == null || root.unwrapParens() is JccTokenReferenceRegexUnit) return

                    // basically if it's a string token it's easy to check

                    if (r.isEmptyMatchPossible()) {
                        holder.registerProblem(
                            root, // report on the root and not on the name
                            makeMessage(o.name, LexicalState.JustDefaultState),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        )
                    }
                }
            }

    companion object {
        const val DisplayName = "Regular expression can match the empty string"

        fun makeMessage(name: String?, states: List<String>): String {
            val statesPart = when {
                states.isEmpty() -> "in all lexical states"
                states.size == 1 -> "in lexical state " + states[0]
                else             -> "in lexical states " + states.joinToString()
            }
            return "Regular expression${name?.let { " for $it" }.orEmpty()} can match the empty string $statesPart. This can result in an endless loop of empty string matches."
        }
    }


}