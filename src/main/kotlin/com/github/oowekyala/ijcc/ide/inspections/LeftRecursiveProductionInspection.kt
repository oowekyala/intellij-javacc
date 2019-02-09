package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.leftMostSet
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
class LeftRecursiveProductionInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription(): String = """
        Reports left-recursive productions. Left-recursion is not supported
        by top-down parsers like JavaCC.
        <p>Possible solutions include right-factoring. That may significantly change
        the parse tree and so break your JJTree structure.</p>
        <!-- tooltip end -->
        <p>This is implemented as an inspection for performance, but it's not an
        "optional error" for JavaCC so I suggest never to turn it off.</p>
    """.trimIndent()

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR

    override fun runForWholeFile(): Boolean = true

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return emptyArray()

        val leftMostSets = file.nonTerminalProductions.associateWith { it.leftMostSet() }
        val visited: VisitStatuses =
                file.nonTerminalProductions.associateWithTo(mutableMapOf()) { VisitStatus.NOT_VISITED }

        val holder = ProblemsHolder(manager, file, isOnTheFly)


        for (prod in leftMostSets.keys) {
            if (visited[prod] != VisitStatus.VISITED) {
                prod.checkLeftRecursion(leftMostSets, visited, immutableListOf(), holder)
            }
        }

        return holder.resultsArray
    }

    private fun JccNonTerminalProduction.checkLeftRecursion(leftMostSets: Map<JccNonTerminalProduction, Set<JccNonTerminalProduction>?>,
                                                            visitStatuses: VisitStatuses,
                                                            loopPath: ProdPath,
                                                            holder: ProblemsHolder) {

        visitStatuses[this] = VisitStatus.BEING_VISITED


        val myLeftMost = leftMostSets[this] ?: return

        val myLoopPath: ProdPath = loopPath.add(this)

        for (prod in myLeftMost) {

            if (visitStatuses[prod] == VisitStatus.BEING_VISITED) {
                val myIdx = myLoopPath.indexOf(prod)
                if (myIdx !in myLoopPath.indices) continue // ??

                // report left recursion
                holder.registerProblem(
                    prod,
                    makeMessage(myLoopPath.subList(myIdx, myLoopPath.size).add(prod)),
                    ProblemHighlightType.ERROR
                )
                break
            }


            prod.checkLeftRecursion(leftMostSets, visitStatuses, loopPath.add(this), holder)
        }

        visitStatuses[this] = VisitStatus.VISITED
    }


    companion object {

        const val DisplayName = "Left-recursive production"

        private fun makeMessage(loopPath: List<JccNonTerminalProduction>) = makeMessageImpl(loopPath.map { it.name })

        @TestOnly
        fun makeMessageImpl(loopPath: List<String>) =
                "Left-recursion detected: " + loopPath.joinToString(separator = " -> ")
    }

}

private typealias ProdPath = ImmutableList<JccNonTerminalProduction>
// unsure means "being visited"
private typealias VisitStatuses = MutableMap<JccNonTerminalProduction, VisitStatus>

enum class VisitStatus {
    NOT_VISITED, VISITED, BEING_VISITED
}
