// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccJavaccOptions
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccJavaccOptionsImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccJavaccOptions {

    override val optionBindingList: List<JccOptionBinding>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccOptionBinding::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitJavaccOptions(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
