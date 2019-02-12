// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.stubs.NonTerminalStub
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil

abstract class JccNonTerminalProductionImpl : JccStubBasedPsiElementImpl<NonTerminalStub>, JccNonTerminalProduction {


    constructor(node: ASTNode) : super(node)

    constructor(stub: NonTerminalStub, stubType: IStubElementType<NonTerminalStub, *>) : super(stub, stubType)


    override fun getName(): String = super<JccNonTerminalProduction>.getName()

    override val javaBlock: JccJavaBlock?
        get() = PsiTreeUtil.getChildOfType(this, JccJavaBlock::class.java)

    override val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?
        get() = PsiTreeUtil.getChildOfType(this, JccJjtreeNodeDescriptor::class.java)

    override val header: JccJavaNonTerminalProductionHeader
        get() = notNullChild<JccJavaNonTerminalProductionHeader>(
            PsiTreeUtil.getChildOfType(
                this,
                JccJavaNonTerminalProductionHeader::class.java
            )
        )

    open fun accept(visitor: JccVisitor) {
        visitor.visitNonTerminalProduction(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getNameIdentifier(): JccIdentifier {
        val p1 = header
        return p1.nameIdentifier
    }

}
