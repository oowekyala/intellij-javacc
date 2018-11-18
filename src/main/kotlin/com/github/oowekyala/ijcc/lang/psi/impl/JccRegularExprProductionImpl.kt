// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccRegularExprProductionImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccRegularExprProduction {
    override fun getNameIdentifier(): PsiElement? = super.nameIdentifier

    override fun getName(): String? = super<JccRegularExprProduction>.getName()

    override val lexicalStateList: JccLexicalStateList?
        get() = findChildByClass(JccLexicalStateList::class.java)

    override val regexprKind: JccRegexprKind
        get() = findNotNullChildByClass(JccRegexprKind::class.java)

    override val regexprSpecList: List<JccRegexprSpec>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccRegexprSpec::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitRegularExprProduction(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
