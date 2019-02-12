package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.util.io.java.AccessModifier

/**
 * @author Clément Fournier
 * @since 1.2
 */
interface NonTerminalStub : StubElement<JccNonTerminalProduction> {

    val methodName: String
    val accessModifier: AccessModifier
    val jjtreeNodeQname: String?
    val isBnf: Boolean
}

class NonTerminalStubImpl(parent: StubElement<*>?,
                          elementType: IStubElementType<out StubElement<*>, *>?,
                          override val methodName: String,
                          override val accessModifier: AccessModifier,
                          override val jjtreeNodeQname: String?,
                          override val isBnf: Boolean)
    : StubBase<JccNonTerminalProduction>(parent, elementType), NonTerminalStub