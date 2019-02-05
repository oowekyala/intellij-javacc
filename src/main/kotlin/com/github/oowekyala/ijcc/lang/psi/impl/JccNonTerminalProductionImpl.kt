// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

abstract class JccNonTerminalProductionImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccNonTerminalProduction {

    override val javaBlock: JccJavaBlock?
        get() = findChildByClass(JccJavaBlock::class.java)

    override val jjtreeNodeDescriptor: JccJjtreeNodeDescriptor?
        get() = findChildByClass(JccJjtreeNodeDescriptor::class.java)

    override val header: JccJavaNonTerminalProductionHeader
        get() = findNotNullChildByClass(JccJavaNonTerminalProductionHeader::class.java)

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

    override fun getName(): String {
        return super<JccNonTerminalProduction>.getName()
    }

}
