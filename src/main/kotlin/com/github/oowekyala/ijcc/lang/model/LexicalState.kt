package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.JccRefRegularExpression
import com.github.oowekyala.ijcc.lang.psi.match
import java.util.*
import java.util.function.Function
import java.util.regex.Matcher
import kotlin.Comparator

/**
 * Represents a lexical state.
 *
 * The JavaCC™ lexical specification is organized into a set of "lexical states".
 * Each lexical state is named with an identifier. There is a standard lexical state
 * called DEFAULT. The generated token manager is at any moment in one of these
 * lexical states. When the token manager is initialized, it starts off in the DEFAULT
 * state, by default. The starting lexical state can also be specified as a parameter
 * while constructing a token manager object.
 *
 * Each lexical state contains an ordered list of regular expressions; the order is
 * derived from the order of occurrence in the input file. There are four kinds of
 * regular expressions: SKIP, MORE, TOKEN, and SPECIAL_TOKEN.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalState private constructor(val name: String, val tokens: List<Token>) {

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
                     stopAtOffset: Int = Int.MAX_VALUE,
                     consideredRegexKinds: Set<RegexKind> = defaultConsideredRegex): Token? =
        if (exact)
            filterWith(consideredRegexKinds, stopAtOffset).firstOrNull { it.matchesLiteral(toMatch) }
        else
            filterWith(consideredRegexKinds, stopAtOffset)
                .mapNotNull { token ->
                    val matcher: Matcher? = token.prefixPattern?.toPattern()?.matcher(toMatch)

                    if (matcher?.matches() == true) Pair(token, matcher.group(0)) else null
                }
                .maxWith(matchComparator)
                ?.let { it.first }

    /**
     * Returns the string token that matches exactly this regex unit.
     *
     * @return the matched token if it was found
     */
    fun matchLiteral(literal: JccLiteralRegexUnit,
                     exact: Boolean,
                     stopAtOffset: Int = Int.MAX_VALUE,
                     consideredRegexKinds: Set<RegexKind> = defaultConsideredRegex): Token? =
        matchLiteral(literal.match, exact, stopAtOffset, consideredRegexKinds)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LexicalState

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    private fun filterWith(consideredRegexKinds: Set<RegexKind>, maxOffset: Int): Sequence<Token> =
        tokens.asSequence()
            .filter { consideredRegexKinds.contains(it.regexKind) }
            .takeWhile { (it.textOffset ?: Int.MAX_VALUE) <= maxOffset }

    companion object {


        private val defaultConsideredRegex = EnumSet.of(RegexKind.TOKEN)

        /**
         * Maximal munch. First take the longest match, then take
         * the highest token in the file.
         */
        private val matchComparator =
            Comparator.comparingInt<Pair<Token, String>> { it.second.length }
                .thenComparing<Token>(Function { it.first }, Token.offsetComparator)

        const val DefaultStateName = "DEFAULT"

        val JustDefaultState = listOf(DefaultStateName)

        class LexicalStateBuilder(val name: String) {

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

            fun build() = LexicalState(name, mySpecs)
        }
    }
}