package com.github.oowekyala.ijcc.lang.psi

interface JccRegexprSpec : JavaccPsiElement {

    val javaBlock: JccJavaBlock?

    val regularExpression: JccRegularExpression

    val lexicalState: JccIdentifier?

}
