// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpressionReference
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.reference.JccTerminalReference
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccRegularExpressionReferenceImpl(node: ASTNode) : JccRegexpUnitImpl(node), JccRegularExpressionReference {

    override fun getReference() = JccTerminalReference(this)

    override fun accept(visitor: JccVisitor) {
        visitor.visitRegularExpressionReference(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getNameIdentifier(): JccIdentifier {
        return findNotNullChildByClass(JccIdentifier::class.java)
    }

}
