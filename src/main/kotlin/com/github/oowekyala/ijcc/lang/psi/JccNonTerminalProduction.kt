// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.stubs.NonTerminalStub
import com.intellij.psi.StubBasedPsiElement

interface JccNonTerminalProduction
    : JccIdentifierOwner, JccNodeClassOwner, JccProduction,
    StubBasedPsiElement<NonTerminalStub> {

    val javaBlock: JccJavaBlock?

    override val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?

    val header: JccJavaNonTerminalProductionHeader

    override fun getNameIdentifier(): JccIdentifier

    override fun getName(): String = stub?.methodName ?: nameIdentifier.name

    @JvmDefault
    val isBnf: Boolean
        get() = stub?.isBnf == true || this is JccBnfProduction

}
