// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccLexicalStateList
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccLexicalStateListImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccLexicalStateList {

    override val identifierList: List<JccIdentifier>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccIdentifier::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitLexicalStateList(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
