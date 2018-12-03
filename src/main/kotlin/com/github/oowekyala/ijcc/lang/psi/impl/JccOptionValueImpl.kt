// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_INTEGER_LITERAL
import com.github.oowekyala.ijcc.lang.JavaccTypes.JCC_STRING_LITERAL
import com.github.oowekyala.ijcc.lang.psi.JccBooleanLiteral
import com.github.oowekyala.ijcc.lang.psi.JccOptionValue
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference

class JccOptionValueImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccOptionValue {

    override val booleanLiteral: JccBooleanLiteral?
        get() = findChildByClass(JccBooleanLiteral::class.java)

    override val integerLiteral: PsiElement?
        get() = findChildByType(JCC_INTEGER_LITERAL)

    override val stringLiteral: PsiElement?
        get() = findChildByType(JCC_STRING_LITERAL)

    override fun getReferences(): Array<PsiReference> {
        return super.getReferences()
    }

    fun accept(visitor: JccVisitor) {
        visitor.visitOptionValue(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
