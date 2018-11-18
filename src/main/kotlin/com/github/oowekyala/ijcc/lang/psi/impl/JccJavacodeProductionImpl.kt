// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccJavaBlock
import com.github.oowekyala.ijcc.lang.psi.JccJavacodeProduction
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccJavacodeProductionImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccJavacodeProduction {
    override fun getNameIdentifier(): PsiElement? = super.nameIdentifier
    override fun getName(): String? = super<JccJavacodeProduction>.getName()

    override val javaBlock: JccJavaBlock
        get() = findNotNullChildByClass(JccJavaBlock::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitJavacodeProduction(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
