package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.addIfNotNull
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInspection.*
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.JBIterable
import gnu.trove.THashSet

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccUnusedProductionInspection : JccInspectionBase(DisplayName) {

    override fun getID(): String = "JavaCCUnusedProduction"

    // TODO technically a rule may be unused from the grammar but folks
    // may use the Java method directly from the parser...
    override fun getStaticDescription() = """
        Detects productions that can't be reached from the root production.
    """.trimIndent()


    override fun runForWholeFile(): Boolean = true

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return null
        if (SuppressionUtil.inspectionResultSuppressed(file, this)) return null
        val prods = JBIterable.from(file.nonTerminalProductions.asIterable())
        if (prods.isEmpty) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)

        //noinspection LimitedScopeInnerClass,EmptyClass
        abstract class Cond<T> : JBIterable.Stateful<Cond<*>>(), Condition<T>

        val inExpr: THashSet<JccNonTerminalProduction> = ContainerUtil.newTroveSet<JccNonTerminalProduction>()
        val inParsing: THashSet<JccNonTerminalProduction> = ContainerUtil.newTroveSet<JccNonTerminalProduction>()
        val inSuppressed: THashSet<JccNonTerminalProduction> = ContainerUtil.newTroveSet<JccNonTerminalProduction>()

        grammarTraverserOnlyBnf(file)
            .filterTypes { it == JccTypes.JCC_NON_TERMINAL_EXPANSION_UNIT }
            .traverse()
            .map { resolveProd(it) }
            .filter(Condition.NOT_NULL)
            .addAllTo(inExpr)

        prods.filter { r -> SuppressionUtil.inspectionResultSuppressed(r, this) }
            .addAllTo(inSuppressed)

        inParsing.add(prods.first()) // add root rule
        var size = 0
        var prev = -1
        while (size != prev) {
            grammarTraverserOnlyBnf(file).expand(object : Cond<PsiElement>() {
                override fun value(element: PsiElement?): Boolean =
                        when (element) {
                            is JccNonTerminalProduction    ->
                                inParsing.contains(element) || inSuppressed.contains(element)
                            is JccNonTerminalExpansionUnit -> {
                                inParsing.addIfNotNull(element.typedReference.resolveProduction())
                                false
                            }
                            else                           -> true
                        }
            }).traverse().size()
            prev = size
            size = inParsing.size
        }



        for (prod in prods.skip(1).filter { o -> !inSuppressed.contains(o) }) {
            when {
                !inExpr.contains(prod)    -> "Unused production"
                !inParsing.contains(prod) -> "Unreachable production"
                else                   -> null
            }?.runIt { message ->
                holder.registerProblem(prod.nameIdentifier, "$message \"${prod.name}\"", ProblemHighlightType.LIKE_UNUSED_SYMBOL)
            }

        }
        return holder.resultsArray
    }

    private fun resolveProd(o: PsiElement?): JccNonTerminalProduction? = when (o) {
        is JccNonTerminalExpansionUnit -> o.typedReference.resolveProduction()
        else                           -> null
    }

    companion object {
        const val DisplayName = "Unused production"
    }
}