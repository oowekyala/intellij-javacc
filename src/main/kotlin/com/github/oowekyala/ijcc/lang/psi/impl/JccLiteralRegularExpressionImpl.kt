// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegularExpression
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccLiteralRegularExpressionImpl(node: ASTNode) : JccRegularExpressionImpl(node), JccLiteralRegularExpression {

    override fun accept(visitor: JccVisitor) {
        visitor.visitLiteralRegularExpression(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getUnit(): JccLiteralRegexUnit = findNotNullChildByClass(JccLiteralRegexUnit::class.java)

    override fun getName(): String = unit.name

    override fun setName(name: String): PsiElement = this.also { unit.name = name }
}
