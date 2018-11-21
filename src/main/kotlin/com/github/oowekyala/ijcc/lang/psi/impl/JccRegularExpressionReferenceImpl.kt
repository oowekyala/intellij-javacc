// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpressionReference
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference

class JccRegularExpressionReferenceImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccRegularExpressionReference {

    override fun getNameIdentifier(): JccIdentifier = findNotNullChildByClass(JccIdentifier::class.java)

    override fun getName(): String? = super<JccRegularExpressionReference>.getName()

    fun accept(visitor: JccVisitor) {
        visitor.visitRegularExpressionReference(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getReference(): PsiReference {
        return super<JccRegularExpressionReference>.getReference()
    }

}
