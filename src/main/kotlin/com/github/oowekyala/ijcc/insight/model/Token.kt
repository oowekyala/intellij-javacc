package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.lang.psi.*

/**
 *
 * There are two places in a grammar files where regular expressions may be written:
 * * Within a regular expression specification (part of a regular expression production).
 *   Those are represented by [ExplicitToken].
 * * As an expansion unit with an expansion. When a regular expression is used in this manner,
 * it is as if the regular expression were defined in the following manner at this location and
 * then referred to by its label from the expansion unit:
 *
 *      <DEFAULT> TOKEN :
 *      {
 *          <genLabel: regex>
 *      }
 *
 * Those are represented by [SyntheticToken].
 *
 * For string tokens, (i.e. literal strings appearing in a BNF expansion as a standalone expansion
 * unit), if an explicit string token exists with the exact same literal, in the same lexical state,
 * then no token is synthesized. This is because JavaCC compacts string tokens s.t. there exists only
 * distinct string tokens. Doing so, it considers IGNORE_CASE expressions as more general. TODO check that
 */
sealed class Token(val regexKind: RegexKind,
                   val lexicalStateNames: List<String>,
                   val isPrivate: Boolean,
                   val name: String?) {

    abstract val regularExpression: JccRegularExpression

    val prefixPattern: Regex? by lazy { regularExpression.prefixPattern }

    @Suppress("LeakingThis")
    val isExplicit: Boolean = this is ExplicitToken

    /**
     * Does a regex match on the string. This is not very useful except if we allow to test
     * regex spec definitions like "Check regexp".
     */
    fun matches(string: String): Boolean = prefixPattern?.matches(string) == true

    /**
     * Returns true if this token is the same literal unit as this one.
     */
    fun matchesLiteral(unit: JccLiteralRegexpUnit): Boolean =
            regularExpression.asSingleLiteral()?.let { unit.match == it.match } == true

}

/**
 * Declared explicitly by the user. The spec is never private.
 */
data class ExplicitToken(val spec: JccRegexprSpec) :
    Token(
        spec.regexKind,
        spec.getLexicalStatesName() ?: LexicalState.JustDefaultState,
        isPrivate = spec.isPrivate,
        name = spec.name
    ) {
    override val regularExpression: JccRegularExpression = spec.regularExpression
}

/**
 * Synthesized by JavaCC.
 */
data class SyntheticToken(val regex: JccRegexpExpansionUnit) : Token(
    RegexKind.TOKEN,
    LexicalState.JustDefaultState,
    isPrivate = false,
    name = regex.regularExpression.name
) {
    override val regularExpression: JccRegularExpression = regex.regularExpression
}