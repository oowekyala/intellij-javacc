// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccTryCatchExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccTryCatchExpansionUnitImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccTryCatchExpansionUnit {

    fun accept(visitor: JccVisitor) {
        visitor.visitTryCatchExpansionUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
