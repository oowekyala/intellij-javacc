package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccTokenReferenceRegexUnit
import com.github.oowekyala.ijcc.lang.psi.descendantSequence
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.ijcc.lang.psi.typedReference
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.TestOnly

/**
 * @author Clément Fournier
 * @since 1.1
 */
class LoopInRegexInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription(): String = """
        Reports loops in regular expressions.
        <!-- tooltip end -->
        <p>This is implemented as an inspection for performance, but it's not an
        "optional error" for JavaCC so I suggest never to turn it off.</p>
    """.trimIndent()

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR

    override fun runForWholeFile(): Boolean = true

    // Like left-recursion checking but simpler, we check for any kind of loop
    // and not just those at start positions

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return emptyArray()

        (file as JccFileImpl).invalidateCachedStructures()
        val grammar = file.lexicalGrammar

        val allTokens = grammar.allTokens

        val visited = allTokens.associateWithTo(mutableMapOf()) { VisitStatus.NOT_VISITED }

        val holder = ProblemsHolder(manager, file, isOnTheFly)


        for (token in allTokens) {
            if (visited[token] != VisitStatus.VISITED) {
                token.checkLoop(visited, immutableListOf(token), holder)
            }
        }

        return holder.resultsArray
    }

    private fun Token.checkLoop(visitStatuses: MutableMap<Token, VisitStatus>,
                                loopPath: ImmutableList<Token>,
                                holder: ProblemsHolder) {

        visitStatuses[this] = VisitStatus.BEING_VISITED

        val myNode = psiElement ?: return

        val myRefs =
            myNode.descendantSequence(includeSelf = true)
                .filterIsInstance<JccTokenReferenceRegexUnit>()


        for (refUnit in myRefs) {

            val reffed = refUnit.typedReference.resolveToken() ?: continue

            if (visitStatuses[reffed] == VisitStatus.BEING_VISITED) {
                val myIdx = loopPath.indexOf(reffed)
                if (myIdx < 0) continue // ??

                // report left recursion
                holder.registerProblem(
                    reffed.psiElement!!,
                    makeMessage(loopPath.subList(myIdx, loopPath.size).add(reffed)),
                    ProblemHighlightType.ERROR
                )
                break
            }


            reffed.checkLoop(visitStatuses, loopPath.add(reffed), holder)
        }

        visitStatuses[this] = VisitStatus.VISITED
    }

    companion object {

        const val DisplayName = "Loop in regular expression"

        private fun makeMessage(loopPath: List<Token>) = makeMessageImpl(loopPath.map { it.name!! })

        @TestOnly
        fun makeMessageImpl(loopPath: List<String>) =
            "Loop detected in regular expression: " + loopPath.joinToString(separator = " -> ")

    }

}
