// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccJavacodeProduction
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.ThreeState

class JccJavacodeProductionImpl(node: ASTNode) : JccNonTerminalProductionImpl(node), JccJavacodeProduction {

    override fun accept(visitor: JccVisitor) {
        visitor.visitJavacodeProduction(this)
    }

    override val isNullable: ThreeState = ThreeState.NO


    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }
}
