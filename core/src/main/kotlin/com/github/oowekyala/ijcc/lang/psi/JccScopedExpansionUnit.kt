// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub
import com.intellij.psi.StubBasedPsiElement

interface JccScopedExpansionUnit
    : JccIdentifierOwner,
    JccExpansionUnit,
    JjtNodeClassOwner,
    StubBasedPsiElement<JccScopedExpansionUnitStub> {

    val expansionUnit: JccExpansionUnit

    override val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor

    override fun getNameIdentifier(): JccIdentifier?

}
