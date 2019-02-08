// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.psi.toPattern
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

abstract class JccRegularExpressionImpl(node: ASTNode) : JccPsiElementImpl(node), JccRegularExpression {

    open fun accept(visitor: JccVisitor) {
        visitor.visitRegularExpression(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }


    override var pattern: Regex? = null
        get() {
            if (field == null) {
                field = toPattern(prefixMatch = false)
            }
            return field
        }

    override val prefixPattern: Regex? by lazy {
        toPattern(prefixMatch = true)
    }

}
