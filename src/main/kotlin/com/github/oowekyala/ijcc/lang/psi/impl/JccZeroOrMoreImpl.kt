// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.psi.JccZeroOrMore
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccZeroOrMoreImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccZeroOrMore {

    fun accept(visitor: JccVisitor) {
        visitor.visitZeroOrMore(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
