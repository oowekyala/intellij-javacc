// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccRegexprSpecImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccRegexprSpec {

    override val identifier: JccIdentifier?
        get() = findChildByClass(JccIdentifier::class.java)

    override val javaBlock: JccJavaBlock?
        get() = findChildByClass(JccJavaBlock::class.java)

    override val regularExpression: JccRegularExpression
        get() = findNotNullChildByClass(JccRegularExpression::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitRegexprSpec(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
