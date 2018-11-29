package com.github.oowekyala.ijcc.model

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.isPrivate
import com.github.oowekyala.ijcc.lang.psi.match
import com.intellij.psi.util.parents
import java.util.*

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
class LexicalState private constructor(val name: String, val tokens: List<JccRegexprSpec>) {


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
    fun matchLiteral(literal: JccLiteralRegularExpression,
                     consideredRegexKinds: Set<RegexKind> = EnumSet.of(RegexKind.TOKEN)): JccRegexprSpec? {

        val toMatch = literal.match
        val regexpContext = literal.parents().firstOrNull { it is JccRegexprSpec } as? JccRegexprSpec

        return tokens.asSequence()
            // remove private regexps if we're not in regex context
            .filterNot { regexpContext == null && it.isPrivate }
            .filter { consideredRegexKinds.contains(it.regexKind) }
            // Stop when below this regex context
            .takeWhile { regexpContext == null || regexpContext !== it.prevSibling }
            .map {
                val matcher = it.prefixPattern?.toPattern()?.matcher(toMatch)

                if (matcher?.matches() == true) Pair(it, matcher.group(0))
                else null
            }
            .filterNotNull()
            .maxBy { it.second.length }
            ?.let { it.first }
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

        const val DefaultStateName = "DEFAULT"

        class LexicalStateBuilder(val name: String) {

            private val specs = mutableListOf<JccRegexprSpec>()

            /** Must be called in document order. */
            fun addToken(token: JccRegexprSpec) {
                specs.add(token)
            }

            fun build() = LexicalState(name, specs)
        }
    }

}