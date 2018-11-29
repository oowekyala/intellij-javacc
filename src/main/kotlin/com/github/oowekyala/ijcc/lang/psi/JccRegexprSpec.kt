package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.RegexKind

interface JccRegexprSpec : JavaccPsiElement {

    val javaBlock: JccJavaBlock?

    val regularExpression: JccRegularExpression

    val lexicalState: JccIdentifier?

    val pattern: Regex?
    val prefixPattern: Regex?
    val regexKind: RegexKind

    val production: JccRegularExprProduction

    /** Returns the list of lexical states this regexp applies to. */
    @JvmDefault
    fun getLexicalStatesName(): List<String>? = production.lexicalStateList?.identifierList?.map { it.name }

    @JvmDefault
    val isPrivate: Boolean
        get() = regularExpression.let { it as? JccNamedRegularExpression }?.isPrivate == true

}
