// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccComplexRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccComplexRegularExpressionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccComplexRegularExpressionImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccComplexRegularExpression {

    override val complexRegularExpressionUnitList: List<JccComplexRegularExpressionUnit>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccComplexRegularExpressionUnit::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitComplexRegularExpression(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
