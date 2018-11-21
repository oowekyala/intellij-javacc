// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_STRING_LITERAL
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccLiteralRegularExpressionImpl(node: ASTNode) : JccRegularExpressionImpl(node), JccLiteralRegularExpression {

    override val stringLiteral: PsiElement
        get() = findNotNullChildByType(JCC_STRING_LITERAL)

    override fun accept(visitor: JccVisitor) {
        visitor.visitLiteralRegularExpression(this)
    }

    override fun getReference() = super<JccLiteralRegularExpression>.getReference()


    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
