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
            element.regularExpression.gatherMatchingLiterals(candidates)

            if (candidates.any { it.textMatches(literal) }) {
                foundSpec = element
                return false
            }
        }

        return true
    }

    // TODO ideally, finding out whether a JccRegularExpression matches a string would be moved to the JccRegularExpression
    private fun JccRegularExpression.gatherMatchingLiterals(result: MutableList<JccLiteralRegularExpression>) {
        when (this) {
            is JccLiteralRegularExpression -> result += this
            is JccNamedRegularExpression   -> if (!isPrivate) {
                regularExpression?.gatherMatchingLiterals(result)
            }
            is JccRegexpSequence           -> if (regularExpressionList.size == 1) {
                regularExpressionList[0].gatherMatchingLiterals(result)
            }
            is JccInlineRegularExpression  ->
                regularExpression?.gatherMatchingLiterals(result)
            is JccRegexpAlternative        -> regularExpressionList.forEach {
                it.gatherMatchingLiterals(result)
            }
        }
    }
}