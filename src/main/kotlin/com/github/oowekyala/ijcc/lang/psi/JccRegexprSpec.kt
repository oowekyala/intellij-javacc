package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.RegexKind
import com.intellij.psi.PsiNamedElement

interface JccRegexprSpec : JavaccPsiElement, PsiNamedElement {

    val javaBlock: JccJavaBlock?

    val regularExpression: JccRegularExpression

    val lexicalState: JccIdentifier?

    val pattern: Regex?
    val prefixPattern: Regex?
    val regexKind: RegexKind

    val production: JccRegularExprProduction

    fun getRegexExpansion(): JccRegexpElement? {
        val regex = regularExpression
        return when (regex) {
            is JccNamedRegularExpression  -> regex.regexpElement
            is JccInlineRegularExpression -> regex.regexpElement
            is JccRegexpElement           -> regex
            else                          -> null // may be a regex reference or something
        }
    }

    /**
     * Return the regex if it's a single literal, unwrapping
     * a [JccNamedRegularExpression] or [JccInlineRegularExpression]
     * if needed.
     */
    @JvmDefault
    fun asSingleLiteral(): JccLiteralRegularExpression? {
        val regex = regularExpression
        return when (regex) {
            is JccLiteralRegularExpression -> regex
            is JccNamedRegularExpression   -> regex.regexpElement as? JccLiteralRegularExpression
            is JccInlineRegularExpression  -> regex.regexpElement as? JccLiteralRegularExpression
            else                           -> null
        }
    }

    /**
     * Returns the root regex element, unwrapping
     * a [JccNamedRegularExpression] or [JccInlineRegularExpression]
     * if needed.
     */
    @JvmDefault
    fun getRootRegexElement(followReferences: Boolean = false): JccRegexpElement? {
        val regex = regularExpression
        return when (regex) {
            is JccLiteralRegularExpression   -> regex
            is JccNamedRegularExpression     -> regex.regexpElement
            is JccRegularExpressionReference -> if (followReferences) regex.reference.resolveToken()?.getRootRegexElement() else null
            is JccInlineRegularExpression    -> regex.regexpElement
            is JccEofRegularExpression       -> null
            else                             -> null
        }
    }

    /** Returns the list of lexical states this regexp applies to. */
    @JvmDefault
    fun getLexicalStatesName(): List<String>? = production.lexicalStateList?.identifierList?.map { it.name }

    @JvmDefault
    val isPrivate: Boolean
        get() = regularExpression.let { it as? JccNamedRegularExpression }?.isPrivate == true

}
