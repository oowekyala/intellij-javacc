package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_INTEGER_LITERAL
import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_STRING_LITERAL
import com.github.oowekyala.ijcc.lang.psi.JccBooleanLiteral
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccOptionBinding
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccOptionBindingImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccOptionBinding {

    override val booleanLiteral: JccBooleanLiteral?
        get() = findChildByClass(JccBooleanLiteral::class.java)

    override val integerLiteral: PsiElement?
        get() = findChildByType(JCC_INTEGER_LITERAL)

    override val stringLiteral: PsiElement?
        get() = findChildByType(JCC_STRING_LITERAL)

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
