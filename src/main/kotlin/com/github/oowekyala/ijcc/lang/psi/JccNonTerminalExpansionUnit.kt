// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.reference.JccNonTerminalReference

interface JccNonTerminalExpansionUnit : JccExpansionUnit, JccIdentifierOwner {

    val javaExpressionList: JccJavaExpressionList?

    override fun getNameIdentifier(): JccIdentifier


    override fun getReference(): JccNonTerminalReference

}
