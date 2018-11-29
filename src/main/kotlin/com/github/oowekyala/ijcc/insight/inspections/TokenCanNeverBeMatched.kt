package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexpAlternative
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.reference.JccStringTokenReference
import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class TokenCanNeverBeMatched : JavaccInspectionBase(DisplayName) {
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
                override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
                    val spec = o.parent as? JccRegexprSpec
                    if (spec != null) {

                    }
                }

                override fun visitRegexprSpec(spec: JccRegexprSpec) {
                    val expansion = spec.getRegexExpansion()
                    if (expansion != null) {
                        when (expansion) {
                            is JccLiteralRegularExpression -> holder.checkRegexElement(
                                spec,
                                expansion,
                                specOwnsProblem = true
                            )
                            is JccRegexpAlternative        -> {
                                expansion.regexpElementList.forEach {
                                    if (it is JccLiteralRegularExpression) {
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
        fun problemDescription(realMatch: JccRegexprSpec) =
                "This token can never be matched, ${realMatch.name} matches its input instead"

        object Provider : InspectionToolProvider {
            override fun getInspectionClasses(): Array<Class<out Any>> =
                    arrayOf(TokenCanNeverBeMatched::class.java)
        }

        fun ProblemsHolder.checkRegexElement(spec: JccRegexprSpec,
                                             elt: JccLiteralRegularExpression,
                                             specOwnsProblem: Boolean) {
            val matchedBy = JccStringTokenReference(elt).resolve() as? JccRegexprSpec
            if (matchedBy != null && matchedBy !== spec) {
                // so it's matched by something different

                val owner = if (specOwnsProblem) spec else elt
                registerProblem(owner, problemDescription(matchedBy), ProblemHighlightType.ERROR)
            }
        }
    }
}