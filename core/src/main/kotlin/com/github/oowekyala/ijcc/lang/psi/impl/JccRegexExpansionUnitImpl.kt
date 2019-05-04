// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccRegexExpansionUnitImpl(node: ASTNode) : JccAssignableExpansionUnitImpl(node), JccRegexExpansionUnit {

    override fun accept(visitor: JccVisitor) {
        visitor.visitRegexExpansionUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getRegularExpression(): JccRegularExpression =
        findNotNullChildByClass(JccRegularExpression::class.java)

    override fun getNameIdentifier(): JccIdentifier? = (regularExpression as? JccNamedRegularExpression)?.nameIdentifier

}
