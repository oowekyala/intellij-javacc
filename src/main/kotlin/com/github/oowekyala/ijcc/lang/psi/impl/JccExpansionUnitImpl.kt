// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccExpansionUnitImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccExpansionUnit {

    override val expansionChoices: JccExpansionChoices?
        get() = findChildByClass(JccExpansionChoices::class.java)

    override val identifier: JccIdentifier?
        get() = findChildByClass(JccIdentifier::class.java)

    override val javaBlock: JccJavaBlock?
        get() = findChildByClass(JccJavaBlock::class.java)

    override val javaExpressionList: JccJavaExpressionList?
        get() = findChildByClass(JccJavaExpressionList::class.java)

    override val localLookahead: JccLocalLookahead?
        get() = findChildByClass(JccLocalLookahead::class.java)

    override val regularExpression: JccRegularExpression?
        get() = findChildByClass(JccRegularExpression::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitExpansionUnit(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
