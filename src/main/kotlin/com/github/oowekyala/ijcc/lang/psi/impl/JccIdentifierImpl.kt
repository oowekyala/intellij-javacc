package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JccTypes.JCC_IDENT
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccIdentifierImpl(node: ASTNode) : JccPsiElementImpl(node), JccIdentifier {

    fun accept(visitor: JccVisitor) {
        visitor.visitIdentifier(this)
    }

    override val leaf: PsiElement
        get() = findNotNullChildByType(JCC_IDENT)

    override fun setName(name: String): PsiElement = replace(JccElementFactory.createIdentifier(project, name))

    override fun getName(): String = text

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor) {
            accept(visitor)
        } else {
            super.accept(visitor)
        }
    }
}
