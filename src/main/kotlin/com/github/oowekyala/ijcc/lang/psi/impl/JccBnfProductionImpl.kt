// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccBnfProduction
import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.ThreeState

class JccBnfProductionImpl(node: ASTNode) : JccNonTerminalProductionImpl(node), JccBnfProduction {


    override var isNullable: ThreeState = ThreeState.UNSURE

    override fun accept(visitor: JccVisitor) {
        visitor.visitBnfProduction(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override val expansion: JccExpansion?
        get() = findChildByClass(JccExpansion::class.java)

}
