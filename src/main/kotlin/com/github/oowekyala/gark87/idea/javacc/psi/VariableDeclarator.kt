package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author gark87
 */
class VariableDeclarator(node: ASTNode) : JavaccStub(node), JavaccDeclarationElement {

    override val identifier: Identifier?
        get() {
            val id = PsiTreeUtil.getChildOfType(this, VariableDeclaratorId::class.java)
                     ?: return null
            return id.identifier
        }

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean = !processor.execute(this, state)
}
