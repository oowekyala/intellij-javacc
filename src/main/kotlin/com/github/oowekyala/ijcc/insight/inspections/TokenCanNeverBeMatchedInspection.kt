package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexpUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexpAlternative
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.refs.JccStringTokenReference
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class TokenCanNeverBeMatchedInspection : JavaccInspectionBase(DisplayName) {
    @Language("HTML")
    override fun getStaticDescription(): String? = """
        Reports tokens or parts of tokens that can never be matched
        because another token takes precedence. For example, if the
        following two specs are in the same lexical state:
        <code>
          &lt;BRACKETS: "[" | "]" >
          &lt;LBRACKET: "[" >
        </code>
        Then LBRACKET can never be matched because BRACKETS <b>matches the
        same input and it's placed above it</b>.If the two are reversed:
        <code>
            &lt;LBRACKET: "[" >
            &lt;BRACKETS: "[" | "]" >
        </code>
        Then BRACKETS can never match the input "[", but will match "]".
    """.trimIndent()


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {

                override fun visitRegexprSpec(spec: JccRegexprSpec) {
                    if (spec.isPrivate) return
                    val expansion = spec.getRootRegexElement(followReferences = false)
                    if (expansion != null) {
                        when (expansion) {
                            is JccLiteralRegexpUnit -> holder.checkRegexElement(
                                spec,
                                expansion,
                                specOwnsProblem = true
                            )
                            is JccRegexpAlternative -> {
                                expansion.regexpElementList.forEach {
                                    if (it is JccLiteralRegexpUnit) {
                                        holder.checkRegexElement(spec, it, specOwnsProblem = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }

    companion object {
        const val DisplayName = "Token can never be matched"
        private fun problemDescription(realMatch: JccRegexprSpec) =
                "This token can never be matched, ${realMatch.name} matches its input instead"

        fun ProblemsHolder.checkRegexElement(spec: JccRegexprSpec,
                                             elt: JccLiteralRegexpUnit,
                                             specOwnsProblem: Boolean) {
            val matchedBy: JccRegexprSpec? = JccStringTokenReference(elt).resolve()
            if (matchedBy != null && matchedBy !== spec) {
                // so it's matched by something different

                val owner = if (specOwnsProblem) spec else elt
                registerProblem(owner, problemDescription(matchedBy), ProblemHighlightType.ERROR)
            }
        }
    }
}