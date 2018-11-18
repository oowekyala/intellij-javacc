// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccJavaFormalParameter
import com.github.oowekyala.ijcc.lang.psi.JccJavaParameterList
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccJavaParameterListImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccJavaParameterList {

    override val javaFormalParameterList: List<JccJavaFormalParameter>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccJavaFormalParameter::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitJavaParameterList(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
