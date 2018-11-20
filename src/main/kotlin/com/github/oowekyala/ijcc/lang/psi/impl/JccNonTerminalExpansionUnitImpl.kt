// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccJavaExpressionList
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference

class JccNonTerminalExpansionUnitImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccNonTerminalExpansionUnit {

    override val identifier: JccIdentifier
        get() = findNotNullChildByClass(JccIdentifier::class.java)

    override val javaExpressionList: JccJavaExpressionList
        get() = findNotNullChildByClass(JccJavaExpressionList::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitNonTerminalExpansionUnit(this)
    }

    override fun getReference(): PsiReference? {
        return super<JccNonTerminalExpansionUnit>.getReference()
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
