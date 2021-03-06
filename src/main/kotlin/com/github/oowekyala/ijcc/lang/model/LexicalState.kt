package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.JccRefRegularExpression
import com.github.oowekyala.ijcc.lang.psi.match
import com.intellij.psi.SmartPsiElementPointer
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.regex.Matcher

/**
 * Represents a lexical state.
 *
 * The JavaCC lexical specification is organized into a set of "lexical states".
 * Each lexical state is named with an identifier. There is a standard lexical state
 * called DEFAULT. The generated token manager is at any moment in one of these
 * lexical states. When the token manager is initialized, it starts off in the DEFAULT
 * state, by default. The starting lexical state can also be specified as a parameter
 * while constructing a token manager object.
 *
 * Each lexical state contains an ordered list of regular expressions; the order is
 * derived from the order of occurrence in the input file. There are four kinds of
 * regular expressions: SKIP, MORE, TOKEN, and SPECIAL_TOKEN. These are represented
 * by the [RegexKind] enum.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalState private constructor(val lexicalGrammar: LexicalGrammar,
                                       val name: String,
                                       val tokens: List<Token>,
                                       private val declarator: SmartPsiElementPointer<JccIdentifier>?) {

    val declarationIdent: JccIdentifier?
        get() = declarator?.element

    // Grammars often have big number of tokens in the default state, which makes
    // match computation suboptimal without caching.
    private val matchedTokenCache = ConcurrentHashMap<MatchParams, Token?>()

    private fun computeMatchInternal(matchParams: MatchParams): Token? {

        val (toMatch, exact, consideredRegexKinds) = matchParams

        return if (exact)
            filterWith(consideredRegexKinds).firstOrNull { it.matchesLiteral(toMatch) }
        else
            filterWith(consideredRegexKinds)
                // Only remove private regex if we're looking for an exact match
                // Duplicate private string tokens should still be reported
                .filter { !it.isPrivate }
                .mapNotNull { token ->
                    val matcher: Matcher? = token.prefixPattern?.toPattern()?.matcher(toMatch)

                    if (matcher?.matches() == true) Pair(token, matcher.group(0)) else null
                }
                .maxWithOrNull(matchComparator)
                ?.let { it.first }
    }

    /**
     * Returns the token that matches the given string in this lexical state.
     *
     * A token is matched as follows: All regular expressions in the current
     * lexical state are considered as potential match candidates. The token
     * manager consumes the maximum number of characters from the input stream
     * possible that match one of these regular expressions. That is, the token
     * manager prefers the longest possible match. If there are multiple longest
     * matches (of the same length), the regular expression that is matched is
     * the one with the earliest order of occurrence in the grammar file.
     *
     * @param toMatch              String to match
     * @param exact                Consider only definitions of string tokens that match exactly this match
     * @param consideredRegexKinds Regex kinds to consider for the match. The default is just [RegexKind.TOKEN]
     *
     * @return the matched token if it was found in this state
     */
    fun matchLiteral(toMatch: String,
                     exact: Boolean,
                     consideredRegexKinds: Set<RegexKind> = RegexKind.JustToken): Token? =
        matchedTokenCache.computeIfAbsent(MatchParams(toMatch, exact, consideredRegexKinds)) {
            this.computeMatchInternal(it)
        }

    /**
     * Returns the string token that matches exactly this regex unit.
     *
     * @return the matched token if it was found
     */
    fun matchLiteral(literal: JccLiteralRegexUnit,
                     exact: Boolean,
                     consideredRegexKinds: Set<RegexKind> = RegexKind.JustToken): Token? =
        matchLiteral(literal.match, exact, consideredRegexKinds)


    val successors: Set<LexicalState> by lazy {
        tokens.asSequence()
            .mapNotNull { it.lexicalStateTransition }
            .mapNotNull { lexicalGrammar.getLexicalState(it) }
            .toSet()
    }

    val predecessors: Set<LexicalState> by lazy {
        lexicalGrammar.lexicalStates.filter { this in it.successors }.toSet()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LexicalState

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "LexicalState($name)"

    private fun filterWith(consideredRegexKinds: Set<RegexKind>): Sequence<Token> =
        tokens.asSequence()
            .filter { consideredRegexKinds.contains(it.regexKind) }


    private data class MatchParams(val toMatch: String,
                                   val exactMatch: Boolean,
                                   val consideredRegexKinds: Set<RegexKind>)


    companion object {


        /**
         * Maximal munch. First take the longest match, then take
         * the highest token in the file.
         */
        private val matchComparator =
            Comparator.comparingInt<Pair<Token, String>> { it.second.length }
                .thenComparing<Token>(Function { it.first }, Token.offsetComparator)

        const val DefaultStateName = "DEFAULT"

        val JustDefaultState = listOf(DefaultStateName)

        /**
         * Builds a lexical state, used by [LexicalGrammar].
         */
        internal class LexicalStateBuilder(val name: String,
                                           private val declarator: SmartPsiElementPointer<JccIdentifier>?) {

            private val mySpecs = mutableListOf<Token>()

            /** Must be called in document order. */
            fun addToken(token: Token) {

                if ( // freestanding regex refs are ignored
                    token.regularExpression !is JccRefRegularExpression
                    // don't add duplicate synthetic tokens in the same state
                    && mySpecs.none { Token.areEquivalent(it, token) }) {

                    mySpecs.add(token)

                }
            }

            val currentSpecs: List<Token>
                get() = mySpecs

            fun build(lexicalGrammar: LexicalGrammar) = LexicalState(lexicalGrammar, name, mySpecs, declarator)
        }
    }
}
