package com.github.oowekyala.ijcc.reference

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

/**
 * Finds the token covering the match of a literal regular expression.
 *
 * TODO consider lexical states
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccStringTokenReferenceProcessor(private val literal: JccLiteralRegularExpression) : PsiScopeProcessor {
    private var foundSpec: JccRegexprSpec? = null
    fun result() = foundSpec

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is JccRegexprSpec) {
            val candidates = mutableListOf<JccLiteralRegularExpression>()
            gatherMatchingLiterals(element.regularExpression, candidates)

            if (candidates.any { it.textMatches(literal) }) {
                foundSpec = element
                return false
            }
        }

        return true
    }

    // TODO ideally, finding out whether a JccRegularExpression matches a string would be moved to the JccRegularExpression
    private fun gatherMatchingLiterals(regex: JccRegularExpression, result: MutableList<JccLiteralRegularExpression>) {
        when (regex) {
            is JccLiteralRegularExpression -> result += regex
            is JccNamedRegularExpression   -> if (!regex.isPrivate) {
                gatherMatchingLiterals(regex.regularExpression, result)
            }
            is JccRegexpSequence           -> if (regex.regularExpressionList.size == 1) {
                gatherMatchingLiterals(regex.regularExpressionList[0], result)
            }
            is JccInlineRegularExpression  ->
                gatherMatchingLiterals(regex.regularExpression, result)
            is JccRegexpAlternative        -> regex.regularExpressionList.forEach {
                gatherMatchingLiterals(it, result)
            }
        }
    }
}