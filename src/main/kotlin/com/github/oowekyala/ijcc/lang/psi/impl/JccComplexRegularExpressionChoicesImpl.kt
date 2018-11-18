// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccComplexRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccComplexRegularExpressionChoices
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccComplexRegularExpressionChoicesImpl(node: ASTNode) : JavaccPsiElementImpl(node),
    JccComplexRegularExpressionChoices {

    override val complexRegularExpressionList: List<JccComplexRegularExpression>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccComplexRegularExpression::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitComplexRegularExpressionChoices(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
