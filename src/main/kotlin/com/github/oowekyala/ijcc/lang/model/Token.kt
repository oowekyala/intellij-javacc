package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.model.LexicalState.Companion.JustDefaultState
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

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
sealed class Token {    // we could have a type parameter here, but I'm too lazy to write Token<*> everywhere


    abstract val psiPointer: SmartPsiElementPointer<out JccRegularExpressionOwner>

    /**
     * Returns the list of lexical states this token applies to.
     * Returns empty if the token applies to all states.
     */
    abstract val lexicalStatesOrEmptyForAll: List<String>
    abstract val lexicalStateTransition: String?
    abstract val regexKind: RegexKind
    abstract val isPrivate: Boolean
    abstract val isIgnoreCase: Boolean

    val isExplicit: Boolean = this is ExplicitToken
    val isSynthetic: Boolean = !isExplicit

    // these all need to be delegated!
    val regularExpression: JccRegularExpression? get() = psiPointer.element?.regularExpression
    val prefixPattern: Regex? get() = regularExpression?.prefixPattern
    val textOffset: Int? get() = psiElement?.textOffset
    val line: Int? get() = psiElement?.lineNumber
    val nameIdentifier get() = regularExpression?.let { it as? JccNamedRegularExpression }?.nameIdentifier
    val name: String? get() = regularExpression?.name

    /** Returns true if this is a single literal token. */
    fun getAsStringToken(): JccLiteralRegexUnit? =
    // this relies on the fact that the reference doesn't use the lexical grammar
        regularExpression?.asSingleLiteral(followReferences = true) // TODO should we unwrap unnecessary parentheses?

    val psiElement: JccRegularExpressionOwner? get() = psiPointer.element


    /**
     * Does a regex match on the string. This is not very useful except if we allow to test
     * regex spec definitions like "Check regex".
     * TODO ignore case
     */
    fun matches(string: String): Boolean =
        prefixPattern?.matches(string) == true

    /**
     * Returns true if this token is the same literal unit as this one,
     * modulo [isIgnoreCase].
     */
    fun matchesLiteral(unit: JccLiteralRegexUnit): Boolean = matchesLiteral(unit.match)

    /**
     * Returns true if this token is the same literal unit as this one,
     * modulo [isIgnoreCase].
     */
    fun matchesLiteral(literalMatch: String): Boolean =
        getAsStringToken()?.let {
            literalMatch.equals(it.match, ignoreCase = isIgnoreCase)
        } == true


    companion object {
        /**
         * Returns whether the two tokens should be reduced. Tokens are different
         * if their name is different (and defined) or they are two string tokens
         * that refer to the same string.
         */
        fun areEquivalent(t1: Token, t2: Token): Boolean {

            if (t1.name != null && t2.name != null && t2.name != t1.name) return false

            val t1Str = t1.getAsStringToken()
            val t2Str = t2.getAsStringToken()

            return t1Str != null
                && t2Str != null
                && (t1.matchesLiteral(t2Str) || t2.matchesLiteral(t1Str))
        }

        /**
         * Compares document offset of two tokens. Tokens defined higher in the file are considered greater.
         */
        val offsetComparator: Comparator<Token> =
            Comparator.comparingInt { -(it.textOffset ?: 0) }

    }

}

/**
 * Declared explicitly by the user.
 */
data class ExplicitToken(override val psiPointer: SmartPsiElementPointer<JccRegexSpec>) : Token() {


    constructor(unit: JccRegexSpec) : this(SmartPointerManager.createPointer(unit))

    val spec: JccRegexSpec?
        get() = psiPointer.element

    // should these be initialised at construction time?
    override val lexicalStateTransition: String? get() = spec?.lexicalStateTransition?.name
    override val regexKind: RegexKind get() = spec?.regexKind ?: RegexKind.TOKEN
    override val isPrivate: Boolean get() = spec?.isPrivate == true
    override val isIgnoreCase: Boolean get() = spec?.isIgnoreCase == true
    override val lexicalStatesOrEmptyForAll: List<String>
        get() = spec?.lexicalStatesOrEmptyForAll ?: JustDefaultState // default?

}

/**
 * Synthesized by JavaCC.
 *
 * @property declUnit The highest (by doc offset) regex that implicitly declares this synthesized token
 */
data class SyntheticToken(override val psiPointer: SmartPsiElementPointer<JccRegexExpansionUnit>) : Token() {


    constructor(unit: JccRegexExpansionUnit) : this(SmartPointerManager.createPointer(unit))

    val declUnit: JccRegexExpansionUnit? get() = psiPointer.element

    // constants for all synthetic tokens
    override val regexKind: RegexKind = RegexKind.TOKEN
    override val isPrivate: Boolean = false
    override val lexicalStatesOrEmptyForAll: List<String> = JustDefaultState
    override val lexicalStateTransition: String? = null
    override val isIgnoreCase: Boolean = false

}