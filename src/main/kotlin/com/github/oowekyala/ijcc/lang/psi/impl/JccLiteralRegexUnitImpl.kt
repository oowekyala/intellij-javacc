// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.JccTypes.JCC_STRING_LITERAL
import com.github.oowekyala.ijcc.lang.psi.JccLiteralRegexUnit
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.github.oowekyala.ijcc.lang.psi.safeReplace
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class JccLiteralRegexUnitImpl(node: ASTNode) : JccRegexUnitImpl(node), JccLiteralRegexUnit {

    override val stringLiteral: PsiElement
        get() = findNotNullChildByType(JCC_STRING_LITERAL)

    override fun accept(visitor: JccVisitor) {
        visitor.visitLiteralRegexUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }


    override fun getName(): String = text

    override fun setName(name: String): PsiElement? =
        safeReplace(JccElementFactory.createRegexElement<JccLiteralRegexUnit>(project, name))
}
