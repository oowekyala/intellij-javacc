package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.github.oowekyala.ijcc.lang.psi.JccOptionValue
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

/**
 * This impl is needed to make getName() return non-null
 */
class JccOptionBindingImpl(node: ASTNode) : JccPsiElementImpl(node), JccOptionBinding {

    override val optionValue: JccOptionValue?
        get() = findChildByClass(JccOptionValue::class.java)


    fun accept(visitor: JccVisitor) {
        visitor.visitOptionBinding(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getNameIdentifier(): JccIdentifier? {
        return findChildByClass(JccIdentifier::class.java)
    }

    override fun getName(): String = node.firstChildNode.text
}
