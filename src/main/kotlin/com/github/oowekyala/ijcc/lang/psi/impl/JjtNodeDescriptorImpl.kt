// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.psi.JjtNodeDescriptor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JjtNodeDescriptorImpl(node: ASTNode) : JavaccPsiElementImpl(node), JjtNodeDescriptor {


    fun accept(visitor: JccVisitor) {
        visitor.visitNodeDescriptor(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
