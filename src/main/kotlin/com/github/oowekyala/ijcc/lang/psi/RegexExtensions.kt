package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.insight.model.RegexKind
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.parents
import com.intellij.psi.util.strictParents
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/*

    Extensions and utilities for the class hierarchy of JccRegularExpression.

 */

private val LOG: Logger = Logger.getInstance("#com.github.oowekyala.ijcc.lang.psi.RegexExtensionsKt")


/** The text matched by this literal regex. */
val JccLiteralRegularExpression.match: String
    get() = stringLiteral.text.removeSurrounding("\"")

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

    override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
        builder.append(Pattern.quote(o.match))
    }

    override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
        val regex = o.regexpElement
        if (regex == null) unresolved = true
        else regex.accept(this)
    }

    override fun visitRegularExpressionReference(o: JccRegularExpressionReference) {
        val ref = o.reference.resolveToken()
        if (ref == null)
            unresolved = true
        else
            ref.getRootRegexElement()?.accept(this@RegexResolutionVisitor)
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
        val occurrenceIndicator = o.lastChildNoWhitespace
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
        val UnicodeStopList = listOf<String>()//"\\u212f")
    }
}


/**
 * Returns true if this regular expression occurs somewhere inside a RegexpSpec.
 * In that case it may reference private regexes.
 */
fun JccRegularExpression.isInRegexContext(): Boolean =
        parentSequence(includeSelf = false).any { it is JccRegexprSpec }

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

// TODO remove?
fun JccRegexprSpec.getLiteralsExactMach() {
    // Gathers literals that match this expression recursively. Returns true whether a construct all
    // expansions of this regex match a literal, false if not.
    fun JccRegexpLike.gatherMatchingLiterals(result: MutableList<JccLiteralRegularExpression>): Boolean =
            when (this) {
                is JccLiteralRegularExpression -> {
                    result += this
                    true
                }
                is JccNamedRegularExpression   -> regexpElement?.gatherMatchingLiterals(result) == true
                is JccInlineRegularExpression  -> regexpElement?.gatherMatchingLiterals(result) == true
                is JccRegexpSequence           ->
                    regexpUnitList.size == 1 && regexpUnitList[0].gatherMatchingLiterals(result)
                is JccRegexpAlternative        -> regexpElementList.all {
                    it.gatherMatchingLiterals(result)
                }
                else                           -> false
            }

    val result = mutableListOf<JccLiteralRegularExpression>()

    this.regularExpression.gatherMatchingLiterals(result)
}
