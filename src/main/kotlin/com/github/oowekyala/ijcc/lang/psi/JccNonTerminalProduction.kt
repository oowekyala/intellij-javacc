// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

interface JccNonTerminalProduction : JavaccPsiElement, JccIdentifierOwner {

    val javaBlock: JccJavaBlock

    val header: JccJavaNonTerminalProductionHeader

    @JvmDefault
    override fun getNameIdentifier(): JccIdentifier = header.nameIdentifier
}
