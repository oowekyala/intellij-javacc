// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_INTEGER_LITERAL
import com.github.oowekyala.ijcc.lang.psi.JccExpansionChoices
import com.github.oowekyala.ijcc.lang.psi.JccLocalLookahead
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccLocalLookaheadImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccLocalLookahead {

    override val expansionChoices: JccExpansionChoices?
        get() = findChildByClass(JccExpansionChoices::class.java)

    override val integerLiteral: PsiElement?
        get() = findChildByType(JCC_INTEGER_LITERAL)

    fun accept(visitor: JccVisitor) {
        visitor.visitLocalLookahead(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
