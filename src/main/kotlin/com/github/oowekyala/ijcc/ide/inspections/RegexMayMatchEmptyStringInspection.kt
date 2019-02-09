package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.isEmptyMatchPossible
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
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

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return emptyArray()

        val grammar = file.lexicalGrammar

        val holder = ProblemsHolder(manager, file, isOnTheFly)

        for (token in grammar.allTokens) {

            if (token.regularExpression?.isEmptyMatchPossible() == true) {
                holder.registerProblem(
                    token.regularExpression!!,
                    makeMessage(token.name, token.lexicalStatesOrEmptyForAll),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                )
            }
        }

        return holder.resultsArray
    }


    companion object {
        const val DisplayName = "Regular expression may match empty string"

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