// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccJavaParameterList
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProductionHeader
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccNonTerminalProductionHeaderImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccNonTerminalProductionHeader {

    override fun getName(): String? = super<JccNonTerminalProductionHeader>.getName()


    override val javaParameterList: JccJavaParameterList
        get() = findNotNullChildByClass(JccJavaParameterList::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitNonTerminalProductionHeader(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
