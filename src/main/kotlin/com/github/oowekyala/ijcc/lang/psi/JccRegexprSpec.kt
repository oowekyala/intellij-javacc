package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.RegexKind

interface JccRegexprSpec : JavaccPsiElement, JccIdentifierOwner {

    val lexicalActions: JccJavaBlock?

    val regularExpression: JccRegularExpression

    val lexicalState: JccIdentifier?

    val pattern: Regex?
    val prefixPattern: Regex?
    val regexKind: RegexKind

    val production: JccRegularExprProduction

}

/**
 * Returns the list of lexical states this regexp applies to.
 * If null then this production applies to the DEFAULT state.
 */
fun JccRegexprSpec.getLexicalStatesName(): List<String>? =
        production.lexicalStateList?.identifierList?.map { it.name }

