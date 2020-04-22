// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.ide.refs.JjtNodePolyReference
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.stubs.JccScopedExpansionUnitStub
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil

class JccScopedExpansionUnitImpl : JjtNodeClassOwnerImpl<JccScopedExpansionUnitStub>, JccScopedExpansionUnit {

    override val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor
        get() = notNullChild<JccJjtreeNodeDescriptor>(
            PsiTreeUtil.getChildOfType(
                this,
                JccJjtreeNodeDescriptor::class.java
            )
        )

    constructor(node: ASTNode) : super(node)

    constructor(stub: JccScopedExpansionUnitStub, stubType: IStubElementType<JccScopedExpansionUnitStub, *>) : super(stub, stubType)

    fun accept(visitor: JccVisitor) {
        visitor.visitScopedExpansionUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getReference(): PsiReference = JjtNodePolyReference(this)

    override val expansionUnit: JccExpansionUnit
        get() = notNullChild<JccExpansionUnit>(PsiTreeUtil.getChildOfType(this, JccExpansionUnit::class.java))

    override fun getNameIdentifier(): JccIdentifier? {
        val p1 = jjtreeNodeDescriptor
        return p1.nameIdentifier
    }

}
