// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_STRING_LITERAL
import com.github.oowekyala.ijcc.lang.psi.JccComplexRegularExpressionChoices
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccRegularExpressionImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccRegularExpression {

    override val complexRegularExpressionChoices: JccComplexRegularExpressionChoices?
        get() = findChildByClass(JccComplexRegularExpressionChoices::class.java)

    override val identifier: JccIdentifier?
        get() = findChildByClass(JccIdentifier::class.java)

    override val stringLiteral: PsiElement?
        get() = findChildByType(JCC_STRING_LITERAL)

    fun accept(visitor: JccVisitor) {
        visitor.visitRegularExpression(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
