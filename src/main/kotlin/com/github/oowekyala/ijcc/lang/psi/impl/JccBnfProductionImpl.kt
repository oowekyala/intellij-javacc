// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.psi.JccExpansionChoices
import com.github.oowekyala.ijcc.lang.psi.JccJavaBlock
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccBnfProductionImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccBnfProduction {


    override fun getNameIdentifier(): PsiElement? = super.nameIdentifier
    override fun getName(): String? = super<JccBnfProduction>.getName()

    override val expansionChoices: JccExpansionChoices
        get() = findNotNullChildByClass(JccExpansionChoices::class.java)

    override val javaBlock: JccJavaBlock
        get() = findNotNullChildByClass(JccJavaBlock::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitBnfProduction(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
