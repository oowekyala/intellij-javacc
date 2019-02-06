package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.model.ExplicitToken
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.ide.refs.JccStringTokenReference
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class TokenCanNeverBeMatchedInspection : JccInspectionBase(DisplayName) {
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
        const val NavigateFixName = "Navigate to matching token"

        fun problemDescription(realMatchName: String?) =
                "This token can never be matched, ${realMatchName ?: "another token"} matches its input instead"

        private fun ProblemsHolder.checkRegexElement(spec: JccRegexprSpec,
                                                     elt: JccLiteralRegexpUnit,
                                                     specOwnsProblem: Boolean
        ) {
            val matchedBy: List<JccRegexprSpec> = JccStringTokenReference(elt)
                .multiResolveToken(exact = false)
                .filterIsInstance<ExplicitToken>()
                // matching itself doesn't count
                .mapNotNull { it.spec }
                .filterNot { it === spec }

            if (matchedBy.isNotEmpty()) {

                // TODO support choosing from several elements when there is conflict
                val first = matchedBy.first()
                val owner = if (specOwnsProblem) spec else elt
                registerProblem(
                    owner,
                    problemDescription(first.name),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    NavigateToOtherTokenFix(SmartPointerManager.createPointer(first))
                )
            }
        }


        class NavigateToOtherTokenFix(private val realMatch: SmartPsiElementPointer<JccRegexprSpec>) : LocalQuickFix {
            override fun getFamilyName(): String = NavigateFixName

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                realMatch.element?.navigate(true)
            }
        }
    }
}