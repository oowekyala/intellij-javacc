package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.lang.psi.JccRegexpExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.regexKind

/**
 *
 * There are two places in a grammar files where regular expressions may be written:
 * * Within a regular expression specification (part of a regular expression production).
 *   This is represented by [ExplicitToken].
 * * As an expansion unit with an expansion. When a regular expression is used in this manner, it is as if the regular expression were defined in the following manner at this location and then referred to by its label from the expansion unit:
 *
 *      <DEFAULT> TOKEN :
 *      {
 *          regular expression
 *      }
 *
 * That is, this usage of regular expression can be rewritten using the other kind of usage. Those are
 * represented by [SyntheticToken]
 */
sealed class Token(val regexKind: RegexKind) : Comparable<Token> {

    abstract val regularExpression: JccRegularExpression

    val pattern: Regex? by lazy { regularExpression.pattern }
    val prefixPattern: Regex? by lazy { regularExpression.prefixPattern }

    /**
     * This token is "lower" than another token if it appears before it in the document.
     */
    override fun compareTo(other: Token): Int =
            regularExpression.textOffset.compareTo(other.regularExpression.textOffset)

}

/**
 * Declared explicitly by the user. The spec is never private.
 */
data class ExplicitToken(val spec: JccRegexprSpec) : Token(spec.regexKind) {
    override val regularExpression: JccRegularExpression = spec.regularExpression
}

/**
 * Synthesized by JavaCC.
 */
data class SyntheticToken(val regex: JccRegexpExpansionUnit) : Token(RegexKind.TOKEN) {
    override val regularExpression: JccRegularExpression = regex.regularExpression
}