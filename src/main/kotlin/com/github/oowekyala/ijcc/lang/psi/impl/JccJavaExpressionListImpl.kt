// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccJavaExpressionList
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccJavaExpressionListImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccJavaExpressionList {

    fun accept(visitor: JccVisitor) {
        visitor.visitJavaExpressionList(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
