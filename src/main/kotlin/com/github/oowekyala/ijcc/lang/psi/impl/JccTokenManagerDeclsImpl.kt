// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccJavaBlock
import com.github.oowekyala.ijcc.lang.psi.JccTokenManagerDecls
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccTokenManagerDeclsImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccTokenManagerDecls {

    override val javaBlock: JccJavaBlock
        get() = findNotNullChildByClass(JccJavaBlock::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitTokenManagerDecls(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
