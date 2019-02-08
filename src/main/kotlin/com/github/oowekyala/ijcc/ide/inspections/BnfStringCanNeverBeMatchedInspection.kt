package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.psi.asSingleLiteral
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.SmartPsiElementPointer
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.1
 */
class BnfStringCanNeverBeMatchedInspection : JccInspectionBase(DisplayName) {
    @Language("HTML")
    override fun getStaticDescription(): String? = """
        Reports usage of literal regular expressions in BNF that cannot
        be matched as their corresponding string token, because another
        token takes precedence.

        <!-- tooltip end -->

        For example, if the following two specs are in the same lexical state:
        <code>
          &lt; BRACKETS: "[" | "]" >
          &lt; LBRACKET: "[" >
        </code>
        Then using "[" in the BNF will never match LBRACKET (its corresponding
        string token), because BRACKETS <b>matches the same input and it's
        placed above it</b>. This also happens if LBRACKET was never defined
        explicitly but instead is implicitly defined by JavaCC when <code>"["</code>
        is first used as an expansion unit. If the two are reversed:
        <code>
            &lt; LBRACKET: "[" >
            &lt; BRACKETS: "[" | "]" >
        </code>
        Then "[" will match LBRACKET correctly.
    """.trimIndent()


    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
            object : JccVisitor() {

                override fun visitRegexExpansionUnit(o: JccRegexExpansionUnit) {
                    val unit = o.regularExpression.asSingleLiteral(followReferences = true) ?: return

                    val defaultState = o.containingFile.lexicalGrammar.defaultState


                    val myStringToken = when (o.regularExpression) {
                        // the named regex may have be in the state but not match because
                        // a reference to it placed above in the file matches instead. A
                        // named regex is however counted as a token.
                        is JccNamedRegularExpression -> SyntheticToken(o)
                        else                         -> defaultState.matchLiteral(unit, exact = true) ?: return // error
                    }
                    val realMatch = defaultState.matchLiteral(unit, exact = false) ?: return

                    if (myStringToken != realMatch) {

                        val myFixes = when {
                            myStringToken.isExplicit -> arrayOf(
                                NavigateToOtherTokenFix(RealNavigateFixName, realMatch.psiPointer),
                                NavigateToOtherTokenFix(NavigateFixName, myStringToken.psiPointer)
                            )
                            else                     ->
                                arrayOf(NavigateToOtherTokenFix(RealNavigateFixName, realMatch.psiPointer))
                        }

                        holder.registerProblem(
                            o,
                            problemDescription(unit.text, myStringToken.name, realMatch.name),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            *myFixes
                        )
                    }
                }
            }

    companion object {
        const val DisplayName = "String will never be matched as a string token"
        const val RealNavigateFixName = "Navigate to real matching token"
        const val NavigateFixName = "Navigate to string token"

        fun problemDescription(literalText: String, stringTokenName: String?, realMatchName: String?) =
                "$literalText cannot be matched as ${stringTokenName?.let { "the string literal token <$it>" }
                    ?: "a string literal token"}, " +
                        "${realMatchName ?: "another token"} matches its input instead"


        class NavigateToOtherTokenFix(private val myname: String,
                                      private val realMatch: SmartPsiElementPointer<out NavigatablePsiElement>)
            : LocalQuickFix {

            override fun getFamilyName(): String = myname

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                realMatch.element?.navigate(true)
            }
        }
    }
}