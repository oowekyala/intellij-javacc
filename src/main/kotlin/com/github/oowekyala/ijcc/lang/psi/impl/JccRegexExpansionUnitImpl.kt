// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccRegexExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNamedElement

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

    override fun getRegularExpression(): JccRegularExpression {
        return findNotNullChildByClass(JccRegularExpression::class.java)
    }

    override fun getName(): String? = regularExpression.name

    override fun setName(name: String): PsiElement {
        val r = regularExpression
        if (r is PsiNamedElement) r.setName(name)
        return this
    }
}
