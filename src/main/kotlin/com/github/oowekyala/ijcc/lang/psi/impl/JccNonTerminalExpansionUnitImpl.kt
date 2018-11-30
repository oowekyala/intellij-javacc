// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccJavaExpressionList
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.reference.JccNonTerminalReference
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccNonTerminalExpansionUnitImpl(node: ASTNode) : JccExpansionUnitImpl(node), JccNonTerminalExpansionUnit {


    override fun getReference(): JccNonTerminalReference = JccNonTerminalReference(this)

    override val javaExpressionList: JccJavaExpressionList?
        get() = findChildByClass(JccJavaExpressionList::class.java)

    override fun accept(visitor: JccVisitor) {
        visitor.visitNonTerminalExpansionUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getNameIdentifier(): JccIdentifier {
        return findNotNullChildByClass(JccIdentifier::class.java)
    }

}
