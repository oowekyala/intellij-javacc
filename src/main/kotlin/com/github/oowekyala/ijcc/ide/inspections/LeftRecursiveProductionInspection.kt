package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.cfa.LeftMostSet
import com.github.oowekyala.ijcc.lang.cfa.leftMostSet
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.typedReference
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import kotlinx.collections.immutable.*
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.TestOnly

/**
 * @author Clément Fournier
 * @since 1.1
 */
class LeftRecursiveProductionInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription(): String = """
        Reports productions that can expand to themselves without consuming
        any tokens. Left-recursion is not supported by top-down parsers like JavaCC,
        because it would cause infinite recursion.
        <p>Possible solutions include right-factoring, but that may significantly
        change your JJTree structure.</p>
        <!-- tooltip end -->
        <p>This is implemented as an inspection for performance, but it's not an
        "optional error" for JavaCC so I suggest never to turn it off.</p>
    """.trimIndent()

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR

    override fun runForWholeFile(): Boolean = true

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return emptyArray()

        val leftMostSets = file.nonTerminalProductions.associateWith { it.leftMostSet() }
        val visited = file.nonTerminalProductions.associateWithTo(mutableMapOf()) { VisitStatus.NOT_VISITED }

        val holder = ProblemsHolder(manager, file, isOnTheFly)


        for (prod in leftMostSets.keys) {
            if (visited[prod] != VisitStatus.VISITED) {
                prod.checkLeftRecursion(leftMostSets, visited, persistentListOf(Pair(prod, null)), holder)
            }
        }

        return holder.resultsArray
    }

    private fun JccNonTerminalProduction.checkLeftRecursion(leftMostSets: Map<JccNonTerminalProduction, LeftMostSet?>,
                                                            visitStatuses: MutableMap<JccNonTerminalProduction, VisitStatus>,
                                                            loopPath: ProductionLoopPath,
                                                            holder: ProblemsHolder) {

        visitStatuses[this] = VisitStatus.BEING_VISITED


        val myLeftMost = leftMostSets[this] ?: persistentSetOf()

        loop@ for (ref in myLeftMost) {

            val prod = ref.typedReference.resolveProduction() ?: run {
                visitStatuses[this] = VisitStatus.VISITED
                return
            }

            when (visitStatuses[prod]) {
                VisitStatus.VISITED       -> break@loop // should we continue?
                null, VisitStatus.NOT_VISITED   ->
                    // recurse
                    prod.checkLeftRecursion(leftMostSets, visitStatuses, loopPath.add(Pair(prod, ref)), holder)
                VisitStatus.BEING_VISITED -> {
                    // then prod is left recursive because it's somewhere on the path

                    val myIdx = loopPath.indexOfFirst { it.first == prod }
                    if (myIdx < 0) continue@loop // ??

                    val subPath: ProductionLoopPath = loopPath.subList(myIdx, loopPath.size).toPersistentList().add(Pair(prod, ref))

                    // report on the root production
                    holder.registerProblem(
                        prod.nameIdentifier,
                        makeMessage(subPath),
                        ProblemHighlightType.ERROR
                    )

                    for ((prodOnCycle, cyclePart) in subPath.removeAt(0)) {
                        cyclePart?.runIt {
                            holder.registerProblem(
                                cyclePart,
                                cyclePartMessage(),
                                ProblemHighlightType.ERROR
                            )
                        }
                        // avoid revisiting those, they're already part of one cycle
                        visitStatuses[prodOnCycle] = VisitStatus.VISITED
                    }
                    break@loop
                }
            }
        }

        visitStatuses[this] = VisitStatus.VISITED
    }


    companion object {

        const val DisplayName = "Left-recursive production"
        private fun makeMessage(loopPath: ProductionLoopPath) = makeMessageImpl(loopPath.map { it.first.name })

        @TestOnly
        fun makeMessageImpl(loopPath: List<String>) =
            "Left-recursion detected: " + loopPath.joinToString(separator = " -> ")

        fun cyclePartMessage() = "Part of a left-recursive cycle"
    }

}

enum class VisitStatus {
    NOT_VISITED, VISITED, BEING_VISITED
}

/**
 * List of visited productions zipped with the expansion unit that
 * added them to the path. Only the first in the list has no corresponding
 * expansion unit.
 */
private typealias ProductionLoopPath = PersistentList<Pair<JccNonTerminalProduction, JccNonTerminalExpansionUnit?>>
