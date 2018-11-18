// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccExpansionChoices
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccExpansionChoicesImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccExpansionChoices {

    override val expansionList: List<JccExpansion>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccExpansion::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitExpansionChoices(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
