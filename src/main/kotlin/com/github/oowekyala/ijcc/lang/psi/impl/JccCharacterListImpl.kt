// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccCharacterDescriptor
import com.github.oowekyala.ijcc.lang.psi.JccCharacterList
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccCharacterListImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccCharacterList {

    override val characterDescriptorList: List<JccCharacterDescriptor>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccCharacterDescriptor::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitCharacterList(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
