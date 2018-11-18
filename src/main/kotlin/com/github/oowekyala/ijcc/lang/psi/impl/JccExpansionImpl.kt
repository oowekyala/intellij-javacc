// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccExpansionImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccExpansion {

    override val expansionUnitList: List<JccExpansionUnit>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccExpansionUnit::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitExpansion(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
