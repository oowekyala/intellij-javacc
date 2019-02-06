package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.insight.model.LexicalState.Companion.JustDefaultState
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.deemsEqual
import com.intellij.psi.PsiElement

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
@Suppress("LeakingThis")
sealed class Token(val regexKind: RegexKind,
                   val isPrivate: Boolean,
                   val name: String?) {

    /**
     * Returns the list of lexical states this token applies to.
     * Returns empty if the token applies to all states.
     */
    abstract val lexicalStatesOrEmptyForAll: List<String>
    abstract val lexicalStateTransition: String?
    abstract val regularExpression: JccRegularExpression

    val prefixPattern: Regex? by lazy { regularExpression.prefixPattern }

    val isExplicit: Boolean = this is ExplicitToken

    /** Returns true if this is a single literal token. */
    val asStringToken: JccLiteralRegexpUnit?
        get() = regularExpression.asSingleLiteral(followReferences = false)

    val psiElement: PsiElement
        get () = when (this) {
            is ExplicitToken  -> spec
            is SyntheticToken -> declUnit
        }

    /**
     * Does a regex match on the string. This is not very useful except if we allow to test
     * regex spec definitions like "Check regexp".
     */
    fun matches(string: String): Boolean = prefixPattern?.matches(string) == true

    /**
     * Returns true if this token is the same literal unit as this one.
     */
    fun matchesLiteral(unit: JccLiteralRegexpUnit): Boolean =
            regularExpression.asSingleLiteral(followReferences = false)?.let { unit.match == it.match } == true


    companion object {

        fun areEquivalent(t1: Token, t2: Token) = stringTokenComparator.deemsEqual(t1, t2)

        /**
         * Comparator that considers two string tokens with the same match equal (compare to 0).
         * Otherwise just compares document offset.
         */
        val stringTokenComparator: Comparator<Token> = Comparator { t1, t2 ->

            val t1Str = t1.asStringToken
            val t2Str = t2.asStringToken

            return@Comparator when {
                t1Str != null && t2Str != null && t1Str.match == t2Str.match -> 0
                else                                                         -> offsetComparator.compare(t1, t2)
            }
        }

        /**
         * Compares document offset of two tokens. Tokens defined higher in the file are considered greater.
         */
        val offsetComparator: Comparator<Token> = Comparator.comparingInt { -it.regularExpression.textOffset }

    }

}

/**
 * Declared explicitly by the user. The spec can be private.
 */
data class ExplicitToken(val spec: JccRegexprSpec)
    : Token(spec.regexKind, isPrivate = spec.isPrivate, name = spec.name) {

    override val lexicalStatesOrEmptyForAll: List<String> = spec.lexicalStatesNameOrEmptyForAll
    override val regularExpression: JccRegularExpression = spec.regularExpression
    override val lexicalStateTransition: String? = spec.lexicalStateTransition?.name
}

/**
 * Synthesized by JavaCC.
 *
 * @property declUnit The highest (by doc offset) regex that implicitly declares this synthesized token
 */
data class SyntheticToken(val declUnit: JccRegexpExpansionUnit) : Token(
    RegexKind.TOKEN,
    isPrivate = false,
    name = declUnit.regularExpression.name
) {
    override val lexicalStatesOrEmptyForAll: List<String> = JustDefaultState
    override val regularExpression: JccRegularExpression = declUnit.regularExpression
    override val lexicalStateTransition: String? = null


    internal val variantsImpl = mutableListOf(declUnit)

    /**
     * All implicit tokens declared in other non-terminals that are equivalent
     * to this one according to [Token.Companion.stringTokenComparator]. This
     * includes [declUnit].
     */
    val variants: List<JccRegexpExpansionUnit> = variantsImpl

}