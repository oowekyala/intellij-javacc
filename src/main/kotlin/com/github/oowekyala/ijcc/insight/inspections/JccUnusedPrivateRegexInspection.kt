package com.github.oowekyala.ijcc.insight.inspections

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.addIfNotNull
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInspection.*
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.JBIterable

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccUnusedPrivateRegexInspection : JavaccInspectionBase(DisplayName) {

    override fun getStaticDescription() = """
        Detects private regex specs that aren't used in any token definition.
        Such regexes don't define a token and as such are unnecessary
    """.trimIndent()


    override fun runForWholeFile(): Boolean = true

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JccFile) return null
        if (SuppressionUtil.inspectionResultSuppressed(file, this)) return null
        val tokens = JBIterable.from(file.globalTokenSpecs.asIterable())
        if (tokens.isEmpty) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)

        //noinspection LimitedScopeInnerClass,EmptyClass
        abstract class Cond<T> : JBIterable.Stateful<Cond<*>>(), Condition<T>

        val inExpr = ContainerUtil.newTroveSet<JccRegexprSpec>()
        val inParsing = ContainerUtil.newTroveSet<JccRegexprSpec>()
        val inSuppressed = ContainerUtil.newTroveSet<JccRegexprSpec>()

        grammarTraverser(file)
            .filterTypes { it == JavaccTypes.JCC_TOKEN_REFERENCE_UNIT }
            .traverse()
            .map { resolveToken(it) }
            .filter(Condition.NOT_NULL)
            .addAllTo(inExpr)


        tokens.filter { r -> SuppressionUtil.inspectionResultSuppressed(r, this) }.addAllTo(inSuppressed)

        inParsing.add(tokens.first()) // add root rule
        var size = 0
        var prev = -1
        while (size != prev) {
            grammarTraverser(file).expand(object : Cond<PsiElement>() {
                override fun value(element: PsiElement?): Boolean =
                        when (element) {
                            is JccRegexprSpec        ->
                                inParsing.contains(element) || inSuppressed.contains(element)
                            is JccTokenReferenceUnit -> {
                                inParsing.addIfNotNull(element.typedReference.resolveToken())
                                false
                            }
                            else                     -> true
                        }
            }).traverse().size()
            prev = size
            size = inParsing.size
        }



        for (spec: JccRegexprSpec in tokens.filter { it.isPrivate }.filter { o -> !inSuppressed.contains(o) }) {
            when {
                !inExpr.contains(spec)    -> "Unused private regex"
                !inParsing.contains(spec) -> "Unreachable private regex"
                else                      -> null
            }?.runIt { message ->
                holder.registerProblem(
                    spec,
                    message,
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    spec.nameTextRange!!.relativize(spec.textRange)!!
                )
            }

        }
        return holder.resultsArray
    }

    private fun resolveToken(o: PsiElement?): JccRegexprSpec? = when (o) {
        is JccTokenReferenceUnit -> o.typedReference.resolveToken()
        else                     -> null
    }

    companion object {
        const val DisplayName = "Unused private regex"
    }
}