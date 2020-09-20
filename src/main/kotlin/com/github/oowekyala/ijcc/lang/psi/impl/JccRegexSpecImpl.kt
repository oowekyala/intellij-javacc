// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor

class JccRegexSpecImpl(node: ASTNode) : JccPsiElementImpl(node), JccRegexSpec {

    fun accept(visitor: JccVisitor) {
        visitor.visitRegexSpec(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

    override fun getRegularExpression(): JccRegularExpression =
        findNotNullChildByClass(JccRegularExpression::class.java)

    override fun getLexicalStateTransition(): JccIdentifier? = findChildByClass(JccIdentifier::class.java)

    override fun getLexicalActions(): JccJavaBlock? = findChildByClass(JccJavaBlock::class.java)

    override fun getJjtreeNodeDescriptor(): JccJjtreeNodeDescriptor? = findChildByClass(JccJjtreeNodeDescriptor::class.java)

    override fun getName(): String? = nameIdentifier?.text

    override fun getNameIdentifier(): JccIdentifier? = (regularExpression as? JccNamedRegularExpression)?.nameIdentifier
}
