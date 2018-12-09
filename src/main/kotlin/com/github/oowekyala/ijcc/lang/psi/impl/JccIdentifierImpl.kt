// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccIdentifierImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccIdentifier {
    // TODO is this needed?
    //    override fun getReference(): PsiReference? = parent?.reference

    fun accept(visitor: JccVisitor) {
        visitor.visitIdentifier(this)
    }

    override fun getName(): String = text

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor) {
            accept(visitor)
        } else {
            super.accept(visitor)
        }
    }
}
