package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.RegexKind
import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.parents
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/*

    Extensions and utilities for the class hierarchy of JccRegexpLike

 */

private val LOG: Logger = Logger.getInstance("#com.github.oowekyala.ijcc.lang.psi.RegexExtensionsKt")


/** The text matched by this literal regex. */
val JccLiteralRegexpUnit.match: String
    get() = stringLiteral.text.removeSurrounding("\"")

/**
 * Converts a regular expression to a [Regex] that may be executed
 * to match text. [PatternSyntaxException] are logged and cause the
 * visit to return null. Any unresolved token reference also causes
 * to return null.
 */
fun JccRegularExpression.toPattern(prefixMatch: Boolean = false): Regex? {
    val root = this
    val visitor = RegexResolutionVisitor(prefixMatch)
    root.accept(visitor)
    return if (visitor.unresolved) null
    else try {
        Regex(visitor.builder.toString())
    } catch (e: PatternSyntaxException) {
        LOG.error(e)
        null
    }
}

/** Returns the regex spec this regex is declared in, or null if this is a regex inside an expansion. */
val JccRegexpLike.specContext: JccRegexprSpec?
    get() = parents().firstOrNull { it is JccRegexprSpec } as? JccRegexprSpec


private class RegexResolutionVisitor(prefixMatch: Boolean) : RegexLikeDFVisitor() {

    val builder = StringBuilder()

    init {
        if (prefixMatch) builder.append('^')
    }

    var unresolved = false

    override fun visitLiteralRegexpUnit(o: JccLiteralRegexpUnit) {
        builder.append(Pattern.quote(o.match))
    }

    override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
        o.unit.acceptChildren(this)
    }

    override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
        val regex = o.regexpElement
        if (regex == null) unresolved = true
        else regex.accept(this)
    }

    override fun visitTokenReferenceUnit(o: JccTokenReferenceUnit) {
        val ref = o.typedReference.resolveToken()
        if (ref == null)
            unresolved = true
        else ref.pattern?.toString().let { builder.append(it) }
    }

    override fun visitRegularExpressionReference(o: JccRegularExpressionReference) {
        o.unit.accept(this)
    }

    override fun visitNamedRegularExpression(o: JccNamedRegularExpression) {
        o.regexpElement?.accept(this)
    }

    override fun visitRegexpSequence(o: JccRegexpSequence) {
        o.regexpUnitList.forEach { it.accept(this) }
    }

    override fun visitRegexpAlternative(o: JccRegexpAlternative) {
        val iterator = o.regexpElementList.iterator()
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

    override fun visitCharacterList(o: JccCharacterList) {
        if (o.isAnyMatch) {
            builder.append(".")
            return
        }
        builder.append('[')
        if (o.isNegated) builder.append('^')
        o.characterDescriptorList.forEach { it.accept(this) }
        builder.append(']')
    }

    override fun visitParenthesizedRegexpUnit(o: JccParenthesizedRegexpUnit) {
        builder.append('(')
        o.regexpElement.accept(this)
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
                    || this == "\\\\"        -> this
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
    get() = nameIdentifier.prevSiblingNoWhitespace?.node?.elementType == JavaccTypes.JCC_POUND

val JccRegexprSpec.isPrivate: Boolean
    get() = regularExpression.let { it as? JccNamedRegularExpression }?.isPrivate == true

/**
 * Gets the text range inside this named regex that includes the name identifier and the
 * pound (#) if it's private.
 */
val JccNamedRegularExpression.nameTextRange: TextRange
    get() =
        this.node.findChildByType(JavaccTypes.JCC_POUND)
            ?.textRange
            ?.let { it.union(nameIdentifier.textRange) }
            ?: nameIdentifier.textRange

val JccRegexprSpec.nameTextRange: TextRange?
    get() = regularExpression.let { it as? JccNamedRegularExpression }?.nameTextRange


/**
 * Return the regex if it's a single literal, unwrapping
 * a [JccNamedRegularExpression] or [JccInlineRegularExpression]
 * if needed.
 */
fun JccRegexprSpec.asSingleLiteral(followReferences: Boolean = false): JccLiteralRegexpUnit? =
        regularExpression.asSingleLiteral(followReferences)


/**
 * Return the regex if it's a single literal, unwrapping
 * a [JccNamedRegularExpression] or [JccInlineRegularExpression]
 * if needed.
 */
fun JccRegularExpression.asSingleLiteral(followReferences: Boolean = false): JccLiteralRegexpUnit? =
        getRootRegexElement(followReferences) as? JccLiteralRegexpUnit

/**
 * Returns the root regex element, unwrapping
 * a [JccNamedRegularExpression] or [JccInlineRegularExpression]
 * if needed.
 */
fun JccRegexprSpec.getRootRegexElement(followReferences: Boolean = false): JccRegexpElement? =
        regularExpression.getRootRegexElement(followReferences)

/**
 * Returns the root regex element, unwrapping
 * a [JccNamedRegularExpression] or [JccInlineRegularExpression]
 * if needed.
 */
fun JccRegularExpression.getRootRegexElement(followReferences: Boolean = false): JccRegexpElement? {
    return when (this) {
        is JccLiteralRegularExpression   -> this.unit
        is JccNamedRegularExpression     -> this.regexpElement
        is JccRegularExpressionReference -> if (followReferences) this.unit.typedReference.resolveToken()?.getRootRegexElement() else null
        is JccInlineRegularExpression    -> this.regexpElement
        else                             -> null
    }
}


/**
 * Returns true if this token reference may reference private regexes.
 */
val JccTokenReferenceUnit.canReferencePrivate: Boolean
    get() = parentSequence(includeSelf = false).firstOrNull { it is JccRegularExpression } !is JccRegularExpressionReference
            || parentSequence(includeSelf = false).any { it is JccRegexprSpec }

val JccCharacterDescriptor.baseCharElement: PsiElement
    get() = firstChild

val JccCharacterDescriptor.toCharElement: PsiElement?
    get() {
        val strings = node.getChildren(TokenSet.create(JavaccTypes.JCC_STRING_LITERAL))
        return if (strings.size < 2) null
        else strings[1].psi
    }


val JccCharacterDescriptor.baseCharAsString: String
    get() = baseCharElement.text.removeSurrounding("\"")

val JccCharacterDescriptor.toCharAsString: String?
    get() = toCharElement?.text?.removeSurrounding("\"")

val JccCharacterList.isNegated
    get() = firstChild.node.elementType == JavaccTypes.JCC_TILDE


/** Converts this node to the enum constant from [RegexKind]. */
val JccRegexprKind.modelConstant: RegexKind
    get() = when (text.trim()) {
        "TOKEN"         -> RegexKind.TOKEN
        "SPECIAL_TOKEN" -> RegexKind.SPECIAL_TOKEN
        "MORE"          -> RegexKind.MORE
        "SKIP"          -> RegexKind.SKIP
        else            -> throw IllegalArgumentException("Unknown regex kind ${text.trim()}")
    }

/** True if this [JccCharacterList] is of the form `~[]`, which matches any character. */
val JccCharacterList.isAnyMatch: Boolean
    get() = this.isNegated && this.characterDescriptorList.isEmpty()
