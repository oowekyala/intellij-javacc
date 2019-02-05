// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccRegexprSpecImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccRegexprSpec {

    fun accept(visitor: JccVisitor) {
        visitor.visitRegexprSpec(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getRegularExpression(): JccRegularExpression {
        return findNotNullChildByClass(JccRegularExpression::class.java)
    }

    override fun getLexicalStateTransition(): JccIdentifier? {
        return findChildByClass(JccIdentifier::class.java)
    }

    override fun getLexicalActions(): JccJavaBlock? {
        return findChildByClass(JccJavaBlock::class.java)
    }


    override fun getName(): String? = nameIdentifier?.text

    override fun getNameIdentifier(): JccIdentifier? = (regularExpression as? JccNamedRegularExpression)?.nameIdentifier
}
