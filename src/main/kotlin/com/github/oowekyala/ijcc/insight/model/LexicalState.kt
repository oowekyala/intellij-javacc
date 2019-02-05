package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexpUnit
import com.github.oowekyala.ijcc.lang.psi.match
import java.util.*
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
     * @param consideredRegexKinds Regex kinds to consider for the match. The default is just [RegexKind.TOKEN]
     *
     * @return the matched token if it was found in this state
     */
    fun matchLiteral(toMatch: String,
                     consideredRegexKinds: Set<RegexKind> = EnumSet.of(RegexKind.TOKEN)): Token? =
            tokens.asSequence()
                .filter { consideredRegexKinds.contains(it.regexKind) }
                .mapNotNull {
                    val matcher: Matcher? = it.prefixPattern?.toPattern()?.matcher(toMatch)

                    if (matcher?.matches() == true) Pair(it, matcher.group(0)) else null
                }
                .maxWith(matchComparator)
                ?.let { it.first }

    /**
     * Returns the token that matches the given string literal in this lexical state.
     *
     * A token is matched as follows: All regular expressions in the current
     * lexical state are considered as potential match candidates. The token
     * manager consumes the maximum number of characters from the input stream
     * possible that match one of these regular expressions. That is, the token
     * manager prefers the longest possible match. If there are multiple longest
     * matches (of the same length), the regular expression that is matched is
     * the one with the earliest order of occurrence in the grammar file.
     *
     * @param literal              String literal to match
     * @param consideredRegexKinds Regex kinds to consider for the match. The default is just [RegexKind.TOKEN]
     *
     * @return the matched token if it was found
     */
    fun matchLiteral(literal: JccLiteralRegexpUnit,
                     consideredRegexKinds: Set<RegexKind> = EnumSet.of(RegexKind.TOKEN)): Token? {
        return matchLiteral(literal.match, consideredRegexKinds)
    }


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


    companion object {

        /**
         * Maximal munch. First take the longest match, then take
         * the highest token in the file.
         */
        private val matchComparator =
                Comparator.comparingInt<Pair<Token, String>> { it.second.length }
                    .thenComparingInt { -it.first.regularExpression.textOffset }

        const val DefaultStateName = "DEFAULT"

        class LexicalStateBuilder(val name: String) {

            private val mySpecs = mutableListOf<Token>()

            /** Must be called in document order. */
            fun addToken(token: Token) {
                mySpecs.add(token)
            }

            val currentSpecs: List<Token>
                get() = mySpecs

            fun build() = LexicalState(name, mySpecs)
        }
    }

}