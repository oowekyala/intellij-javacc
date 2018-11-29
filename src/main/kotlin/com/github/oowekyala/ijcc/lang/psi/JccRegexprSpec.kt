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


}
