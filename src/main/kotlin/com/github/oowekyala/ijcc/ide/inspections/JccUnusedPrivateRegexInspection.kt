package com.github.oowekyala.ijcc.ide.inspections

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.model.ExplicitToken
import com.github.oowekyala.ijcc.lang.model.Token
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
import org.bouncycastle.asn1.x500.style.RFC4519Style.o
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
        val tokens = file.lexicalGrammar.allTokens.toList()
        if (tokens.isEmpty()) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)


        // specs used in other specs or regular expressions
        val inExpr: THashSet<Token> = ContainerUtil.newTroveSet()
        // specs used in other
        val reachable: THashSet<Token> = ContainerUtil.newTroveSet()
        val inSuppressed: THashSet<Token> = ContainerUtil.newTroveSet()

        grammarTraverser(file)
            .filterTypes { it == JccTypes.JCC_TOKEN_REFERENCE_REGEX_UNIT }
            .traverse()
            .map { resolveToken(it) }
            .filter(Condition.NOT_NULL)
            .addAllTo(inExpr)


        tokens.filter { r -> r.psiElement?.let { SuppressionUtil.inspectionResultSuppressed(it, this) } == true }
            .let { inSuppressed.addAll(it) }

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
                                is JccRegexSpec               -> ExplicitToken(element).let {
                                    reachable.contains(it) || inSuppressed.contains(it)
                                }
                                is JccTokenReferenceRegexUnit -> {
                                    reachable.addIfNotNull(element.typedReference.resolveToken())
                                    false
                                }
                                else                          -> true
                            }
                }).traverse().size()
            prev = size
            size = reachable.size
        }



        for (token: ExplicitToken in tokens.filter { o -> !inSuppressed.contains(o) }.filterIsInstance<ExplicitToken>()) {
            when {
                !inExpr.contains(token)    -> "Unused private regex"
                !reachable.contains(token) -> "Unreachable private regex"
                else                      -> null
            }?.runIt { message ->
                val spec = token.spec ?: return@runIt
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

    private fun resolveToken(o: PsiElement?): Token? = when (o) {
        is JccTokenReferenceRegexUnit -> o.typedReference.resolveToken()
        else                          -> null
    }

    companion object {
        const val DisplayName = "Unused private regex"
    }
}