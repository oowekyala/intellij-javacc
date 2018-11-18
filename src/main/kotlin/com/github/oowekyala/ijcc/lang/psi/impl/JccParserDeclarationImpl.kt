// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccJavaCompilationUnit
import com.github.oowekyala.ijcc.lang.psi.JccParserDeclaration
import com.github.oowekyala.ijcc.lang.psi.JccVisitor
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil

class JccParserDeclarationImpl(node: ASTNode) : JavaccPsiElementImpl(node), JccParserDeclaration {

    override val identifierList: List<JccIdentifier>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JccIdentifier::class.java)

    override val javaCompilationUnit: JccJavaCompilationUnit
        get() = findNotNullChildByClass(JccJavaCompilationUnit::class.java)

    fun accept(visitor: JccVisitor) {
        visitor.visitParserDeclaration(this)
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JccVisitor)
            accept(visitor)
        else
            super.accept(visitor)
    }

}
