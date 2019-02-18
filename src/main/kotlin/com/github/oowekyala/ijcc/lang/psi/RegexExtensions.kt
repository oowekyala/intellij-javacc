package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JccTypes
import com.github.oowekyala.ijcc.lang.model.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory.createRegex
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/*

    Extensions and utilities for the class hierarchy of JccRegexLike

 */


val JccRegularExpression.pattern: Regex?
    get() = toPattern(prefixMatch = false)

val JccRegularExpression.prefixPattern: Regex?
    get() = toPattern(prefixMatch = true)


/**
 * Converts a regular expression to a [Regex] that may be executed
 * to match text. [PatternSyntaxException] are logged and cause the
 * visit to return null. Any unresolved token reference also causes
 * to return null.
 */
private fun JccRegularExpression.toPattern(prefixMatch: Boolean = false): Regex? =
    toPatternImpl(prefixMatch, mutableSetOf())

private fun JccRegularExpression.toPatternImpl(prefixMatch: Boolean = false,
                                               visited: MutableSet<JccTokenReferenceRegexUnit>): Regex? {
    val visitor = RegexResolutionVisitor(prefixMatch, visited)
    this.accept(visitor)
    return if (visitor.unresolved) null
    else visitor.builder.toString().toRegexSafe()
}

private fun String.toRegexSafe(): Regex? =
    try {
        Regex(this)
    } catch (e: PatternSyntaxException) {
        null
    }

/** Returns the token this regex is declared in. Is synthetic if the regex occurs in a BNF expansion. */
val JccRegexLike.enclosingToken: Token
    get() {
        val enclosingRegex = ancestors(includeSelf = true).first { it is JccRegularExpression }

        return when (val parent = enclosingRegex.parent) {
            is JccRegexSpec          -> ExplicitToken(parent)
            is JccRegexExpansionUnit -> SyntheticToken(parent)
            else                     -> throw IllegalStateException("No enclosing context?")
        }
    }

/**
 * Container regular expressions may be unclosed because of the way we parse
 * them. E.g. when editing, `<` is a container, `<a` is a ref, `<a:` is a named
 * regex. Inspections that inspect container regular expressions should not
 * report unclosed regexes, otherwise they would report incomplete types that
 * were meant to type something else.
 */
val JccContainerRegularExpression.isUnclosed: Boolean
    get() = regexElement.let {
        it == null || it == this.astChildrenSequence(reversed = true).filter { !it.isError }.firstOrNull()
    }

/** The text matched by this literal regex. */
val JccLiteralRegexUnit.match: String
    get() = StringEscapeUtils.unescapeJava(stringLiteral.text.removeSurrounding("\""))

/** The text matched by this literal regex. */
val JccLiteralRegularExpression.match: String
    get() = unit.match

/**
 * Returns the list of lexical states this regex applies to.
 * If empty then this regex applies to all states (<*>).
 *
 */
val JccRegexSpec.lexicalStatesOrEmptyForAll: List<String>
    get() = production.lexicalStatesNameOrEmptyForAll

val JccRegexSpec.production
    get() = parent as JccRegexProduction

val JccRegexSpec.regexKind: RegexKind
    get() = production.regexKind.modelConstant

val JccRegexProduction.lexicalStatesNameOrEmptyForAll: List<String>
    get() = lexicalStateList?.identifierList?.map { it.name } ?: LexicalState.JustDefaultState

/**
 * Lexical states explicitly declared on this regex production.
 * Empty list if it's defaulted to DEFAULT or if it's for all states.
 */
val JccRegexProduction.lexicalStatesIdents: List<JccIdentifier>
    get() = lexicalStateList?.identifierList ?: emptyList()

val JccRegexSpec.isIgnoreCase: Boolean
    get() = production.isIgnoreCase

val JccRegexProduction.isIgnoreCase: Boolean
    get() = regexKind.siblingSequence(forward = true)
        .takeWhile { !it.isOfType(JccTypes.JCC_COLON, JccTypes.JCC_LBRACE) }
        .any { it.isOfType(JccTypes.JCC_IGNORE_CASE_OPTION) }

/**
 * Converts a [JccRegexElement] to an executable regular expression.
 */
private class RegexResolutionVisitor(prefixMatch: Boolean,
                                     private val visited: MutableSet<JccTokenReferenceRegexUnit>) :
    RegexLikeDFVisitor() {

    val builder = StringBuilder()

    init {
        if (prefixMatch) builder.append('^')
    }

    var unresolved = false

    override fun visitLiteralRegexUnit(o: JccLiteralRegexUnit) {
        builder.append(Pattern.quote(o.match))
    }

    override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
        o.unit.accept(this)
    }

    override fun visitContainerRegularExpression(o: JccContainerRegularExpression) {
        val regex = o.regexElement
        if (regex == null) unresolved = true
        else regex.accept(this)
    }

    override fun visitTokenReferenceRegexUnit(o: JccTokenReferenceRegexUnit) {
        if (unresolved || o in visited) {
            unresolved = true // cyclic reference
            return
        }
        visited += o // add the token ref
        val ref = o.typedReference.resolveToken()
        if (ref == null)
            unresolved = true
        else ref.regularExpression
            ?.toPatternImpl(false, visited)
            ?.toString()
            ?.let { builder.append(it) }
            ?: run {
                unresolved = true
            }
    }

    override fun visitRefRegularExpression(o: JccRefRegularExpression) {
        o.unit.accept(this)
    }

    override fun visitNamedRegularExpression(o: JccNamedRegularExpression) {
        o.regexElement?.accept(this)
    }

    override fun visitRegexSequenceElt(o: JccRegexSequenceElt) {
        o.regexUnitList.forEach { it.accept(this) }
    }

    override fun visitRegexAlternativeElt(o: JccRegexAlternativeElt) {
        val iterator = o.regexElementList.iterator()
        // we know there's at least two

        iterator.next().accept(this)
        while (iterator.hasNext()) {
            builder.append('|')
            iterator.next().accept(this)
        }
    }

    override fun visitEofRegularExpression(o: JccEofRegularExpression) {
        builder.append("$")
    }

    override fun visitCharacterListRegexUnit(o: JccCharacterListRegexUnit) {
        if (o.isAnyMatch) {
            builder.append(".")
            return
        }
        builder.append('[')
        if (o.isNegated) builder.append('^')
        o.characterDescriptorList.forEach { it.accept(this) }
        builder.append(']')
    }

    override fun visitParenthesizedRegexUnit(o: JccParenthesizedRegexUnit) {
        builder.append('(')
        o.regexElement.accept(this)
        builder.append(')')
        val occurrenceIndicator = o.occurrenceIndicator
        if (occurrenceIndicator != null) {
            builder.append(occurrenceIndicator.text)
        }
    }

    override fun visitCharacterDescriptor(o: JccCharacterDescriptor) {
        fun String.quoteRegexChar(): String = when {
            length == 1                      -> Pattern.quote(this)
            this == "\\n"
                || this == "\\r"
                || this == "\\f"
                || this == "\\b"
                || this == "\\a"
                || this == "\\e"
                || this == "\\\\"            -> this
            this.matches(UnicodeStringRegex) -> "\\x{" + this.removePrefix("\\u") + "}"
            else                             -> this // ??
        }

        val base = o.baseCharAsString
        val toChar = o.toCharAsString
        // ignore chars in the stoplist, they cause PatternSyntaxExceptions
        if (UnicodeStopList.contains(base) || toChar?.let { UnicodeStopList.contains(it) } == true) return

        builder.append('[')
        builder.append(base.quoteRegexChar())
        if (toChar != null) {
            builder.append("-").append(toChar.quoteRegexChar())
        }
        builder.append(']')
    }

    private companion object {
        val UnicodeStringRegex = Regex("""\\u[0-9a-fA-F]{4}""")
        // this is a last resort to prevent some chars to throw PatternSyntaxExceptions
        // it doesn't look like it's needed rn
        val UnicodeStopList = listOf<String>() //"\\u212f")
    }
}

/**
 * Returns true if this regex is private, ie if it mentions the "#" before its name.
 * Such regular expressions may not be referred to from expansion units, but only
 * from within other regular expressions.  Private regular expressions are not matched
 * as tokens by the token manager. Their purpose is solely to facilitate the definition
 * of other more complex regular expressions.
 */
val JccNamedRegularExpression.isPrivate: Boolean
    get() = nameIdentifier.prevSiblingNoWhitespace?.isOfType(JccTypes.JCC_POUND) == true


val JccRegexSpec.isPrivate: Boolean
    get() = regularExpression.let { it as? JccNamedRegularExpression }?.isPrivate == true


val JccRegularExpression.isPrivate: Boolean
    get() = this is JccNamedRegularExpression && isPrivate


/**
 * Gets the text range inside this named regex that includes the name identifier and the
 * pound (#) if it's private.
 */
val JccNamedRegularExpression.nameTextRange: TextRange
    get() =
        this.node.findChildByType(JccTypes.JCC_POUND)
            ?.textRange
            ?.let { it.union(nameIdentifier.textRange) }
            ?: nameIdentifier.textRange

val JccRegexSpec.nameTextRange: TextRange?
    get() = regularExpression.let { it as? JccNamedRegularExpression }?.nameTextRange

/**
 * Gets the first value of the range. It may be null because the pin
 * is on the brace.
 */
val JccRepetitionRange.first: Int?
    get() = childrenSequence().firstOrNull { it.isOfType(JccTypes.JCC_INTEGER_LITERAL) }?.text?.toInt()

/**
 * Returns true if this literal regex unit is eligible to be a string
 * token, in which case a [typedReference] is available. The opposite of [asSingleLiteral].
 */
val JccLiteralRegexUnit.isStringToken: Boolean
    get() = parent is JccLiteralRegularExpression // common case
        || firstAncestorOrNull<JccRegularExpression>()?.asSingleLiteral(followReferences = false) == this

val JccIdentifier.namedTokenDef: JccRegularExpressionOwner?
    get() = parent.let { it as? JccNamedRegularExpression }?.let { it.parent as? JccRegularExpressionOwner }


/**
 * Return the regex if it's a single literal, unwrapping
 * a [JccNamedRegularExpression] or [JccContainerRegularExpression]
 * if needed.
 */
fun JccRegexSpec.asSingleLiteral(followReferences: Boolean = false): JccLiteralRegexUnit? =
    regularExpression.asSingleLiteral(followReferences)


/**
 * Return the regex if it's a single literal, unwrapping
 * a [JccNamedRegularExpression] or [JccContainerRegularExpression]
 * if needed.
 */
fun JccRegularExpression.asSingleLiteral(followReferences: Boolean = false): JccLiteralRegexUnit? =
    getRootRegexElement(followReferences) as? JccLiteralRegexUnit

/**
 * Returns the root regex element, unwrapping
 * a [JccNamedRegularExpression] or [JccContainerRegularExpression]
 * if needed.
 */
fun JccRegexSpec.getRootRegexElement(followReferences: Boolean = false): JccRegexElement? =
    regularExpression.getRootRegexElement(followReferences)

/**
 * Returns the root regex element, unwrapping a [JccNamedRegularExpression] or [JccContainerRegularExpression]
 * if needed. Also unwraps [JccParenthesizedRegexUnit]s occurring at the top.
 *
 * May return null only if this is a [JccEofRegularExpression].
 *
 * If [followReferences]=true and the root is a [JccTokenReferenceRegexUnit], then the returned element
 * is the deepest element that could be found by following indirections, which may itself be a
 * [JccTokenReferenceRegexUnit] in case of either a reference cycle, or an unresolved token name.
 */
fun JccRegularExpression.getRootRegexElement(followReferences: Boolean = false,
                                             unwrapParens: Boolean = true): JccRegexElement? =
    getRootRegexElementImpl(followReferences, unwrapParens, mutableSetOf())

private fun JccRegularExpression.getRootRegexElementImpl(followReferences: Boolean,
                                                         unwrapParens: Boolean,
                                                         visited: MutableSet<JccTokenReferenceRegexUnit>): JccRegexElement? {

    return when (this) {
        is JccNamedRegularExpression     -> this.regexElement
        is JccRefRegularExpression       -> this.unit
        is JccContainerRegularExpression -> this.regexElement
        is JccEofRegularExpression       -> null
        is JccLiteralRegularExpression   -> this.unit
        else                             -> throw IllegalStateException(this.toString())
    }?.let {
        if (unwrapParens) it.unwrapParens() else it
    }?.let {
        when (it) {
            is JccTokenReferenceRegexUnit ->
                if (followReferences) {
                    if (it in visited) {
                        return it // cyclic reference
                    }

                    visited += it
                    val reffed = it.typedReference.resolveToken()?.regularExpression

                    return when (reffed) {
                        null -> it
                        else -> reffed.getRootRegexElementImpl(followReferences, unwrapParens, visited)
                    }
                } else it
            else                          -> it
        }
    }
}

/**
 * Unwraps unnecessary (no occurrence indicator) parentheses on a regex
 * element and returns the innermost significant element. This is only
 * safe on a place where we know undecorated parentheses are unnecessary,
 * e.g. on the root element.
 */
fun JccRegexElement.unwrapParens(): JccRegexElement = when {
    this is JccParenthesizedRegexUnit
        && this.occurrenceIndicator == null -> this.regexElement.unwrapParens()
    else                                    -> this
}


/**
 * Returns true if this token reference may reference private regexes.
 */
val JccTokenReferenceRegexUnit.canReferencePrivate: Boolean
    get() = ancestors(includeSelf = false).firstOrNull { it is JccRegularExpression } !is JccRefRegularExpression
        || ancestors(includeSelf = false).any { it is JccRegexSpec }

val JccCharacterDescriptor.baseCharElement: PsiElement
    get() = firstChild

val JccCharacterDescriptor.toCharElement: PsiElement?
    get() {
        val strings = node.getChildren(TokenSet.create(JccTypes.JCC_STRING_LITERAL))
        return if (strings.size < 2) null
        else strings[1].psi
    }


val JccCharacterDescriptor.baseCharAsString: String
    get() = baseCharElement.text.removeSurrounding("\"")

val JccCharacterDescriptor.toCharAsString: String?
    get() = toCharElement?.text?.removeSurrounding("\"")

val JccCharacterListRegexUnit.isNegated
    get() = firstChild.node.elementType == JccTypes.JCC_TILDE


/** Converts this node to the enum constant from [RegexKind]. */
val JccRegexKind.modelConstant: RegexKind
    get() = when (text.trim()) {
        "TOKEN"         -> RegexKind.TOKEN
        "SPECIAL_TOKEN" -> RegexKind.SPECIAL_TOKEN
        "MORE"          -> RegexKind.MORE
        "SKIP"          -> RegexKind.SKIP
        else            -> throw IllegalArgumentException("Unknown regex kind ${text.trim()}")
    }

/** True if this [JccCharacterListRegexUnit] is of the form `~[]`, which matches any character. */
val JccCharacterListRegexUnit.isAnyMatch: Boolean
    get() = this.isNegated && this.characterDescriptorList.isEmpty()


/**
 * Promotes a regex element to a regular expression, wrapping it into angled
 * brackets if needed.
 */
fun JccRegexElement.promoteToRegex(): JccRegularExpression = when (this) {
    is JccTokenReferenceRegexUnit -> createRegex<JccRefRegularExpression>(project, text)
    is JccLiteralRegexUnit        -> createRegex<JccLiteralRegularExpression>(project, text)
    else                          -> createRegex<JccContainerRegularExpression>(project, "< $text >")
}

// constrain the hierarchies to be the same to avoid some confusions

fun JccExpansionUnit.safeReplace(regex: JccExpansionUnit) = replace(regex)

inline fun <reified T : JccRegularExpression> JccRegularExpression.safeReplace(regex: T): T = replace(regex) as T

/**
 * Replaces this regex element with the given one, taking care of replacing
 * the parent if it's a [JccRegularExpression].
 */
fun JccRegexElement.safeReplace(regex: JccRegexElement): PsiElement? {
    val parent = parent
    return when {
        regex is JccTokenReferenceRegexUnit
            && parent is JccRegularExpression
            && parent !is JccRefRegularExpression     ->
            parent.safeReplace(regex.promoteToRegex())

        regex is JccLiteralRegexUnit
            && parent is JccRegularExpression
            && parent !is JccLiteralRegularExpression ->
            parent.safeReplace(regex.promoteToRegex())

        else                                          -> replace(regex)
    }
}















