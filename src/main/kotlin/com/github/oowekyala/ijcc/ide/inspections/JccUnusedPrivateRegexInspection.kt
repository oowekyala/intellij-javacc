package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.addIfNotNull
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInspection.*
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Conditions
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.JBIterable
import gnu.trove.THashSet
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccUnusedPrivateRegexInspection : JccInspectionBase(DisplayName) {

    @Language("HTML")
    override fun getStaticDescription() = """
        Detects private regex specs that aren't used in any token definition
        or regex expansion unit. Such regexes don't define a token and are
        unnecessary.
    """.trimIndent()


    override fun runForWholeFile(): Boolean = true

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return null
        if (SuppressionUtil.inspectionResultSuppressed(file, this)) return null
        val tokens = JBIterable.from(file.globalTokenSpecs.asIterable())
        if (tokens.isEmpty) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)


        // specs used in other specs or regular expressions
        val inExpr: THashSet<JccRegexSpec> = ContainerUtil.newTroveSet<JccRegexSpec>()
        // specs used in other
        val reachable: THashSet<JccRegexSpec> = ContainerUtil.newTroveSet<JccRegexSpec>()
        val inSuppressed: THashSet<JccRegexSpec> = ContainerUtil.newTroveSet<JccRegexSpec>()

        grammarTraverser(file)
            .filterTypes { it == JccTypes.JCC_TOKEN_REFERENCE_REGEX_UNIT }
            .traverse()
            .map { resolveToken(it) }
            .filter(Condition.NOT_NULL)
            .addAllTo(inExpr)


        tokens.filter { r -> SuppressionUtil.inspectionResultSuppressed(r, this) }.addAllTo(inSuppressed)

        //noinspection LimitedScopeInnerClass,EmptyClass
        abstract class Cond<T> : JBIterable.Stateful<Cond<*>>(), Condition<T>

        reachable.addAll(tokens.filter { !it.isPrivate })
        var size = 0
        var prev = -1
        while (size != prev) {
            grammarTraverser(file)
                .filter(Conditions.instanceOf(JccRegularExpression::class.java))
                .expand(object : Cond<PsiElement>() {
                    override fun value(element: PsiElement?): Boolean =
                            when (element) {
                                is JccRegexSpec                                                  ->
                                    reachable.contains(element) || inSuppressed.contains(element)
                                is JccTokenReferenceRegexUnit -> {
                                    reachable.addIfNotNull(element.typedReference.resolveToken())
                                    false
                                }
                                else                                                             -> true
                            }
                }).traverse().size()
            prev = size
            size = reachable.size
        }



        for (spec: JccRegexSpec in tokens.filter { it.isPrivate }.filter { o -> !inSuppressed.contains(o) }) {
            when {
                !inExpr.contains(spec)    -> "Unused private regex"
                !reachable.contains(spec) -> "Unreachable private regex"
                else                      -> null
            }?.runIt { message ->
                holder.registerProblem(
                    spec,
                    "$message \"${spec.name}\"",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    spec.nameTextRange!!.relativize(spec.textRange)!!
                )
            }

        }
        return holder.resultsArray
    }

    private fun resolveToken(o: PsiElement?): JccRegexSpec? = when (o) {
        is JccTokenReferenceRegexUnit -> o.typedReference.resolveToken()
        else                                                             -> null
    }

    companion object {
        const val DisplayName = "Unused private regex"
    }
}