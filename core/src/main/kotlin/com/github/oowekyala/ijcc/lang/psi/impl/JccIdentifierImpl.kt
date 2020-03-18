package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.ide.refs.JccLexicalStateReference
import com.github.oowekyala.ijcc.ide.refs.JjtNodePolyReference
import com.github.oowekyala.ijcc.lang.JccTypes.JCC_IDENT
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference

class JccIdentifierImpl(node: ASTNode) : JccPsiElementImpl(node), JccIdentifier {

    fun accept(visitor: JccVisitor) {
        visitor.visitIdentifier(this)
    }

    override val leaf: PsiElement
        get() = findNotNullChildByType(JCC_IDENT)

    override fun setName(name: String): PsiElement = replace(project.jccEltFactory.createIdentifier(name))

    override fun getName(): String = text

    override fun getReference(): PsiReference? = when {
        isJjtreeNodeIdentifier -> this.firstAncestorOrNull<JjtNodeClassOwner>()?.let { JjtNodePolyReference(it) }
        isLexicalStateName     -> JccLexicalStateReference(this)
        else                   -> null
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor) {
            accept(visitor)
        } else {
            super.accept(visitor)
        }
    }
}
