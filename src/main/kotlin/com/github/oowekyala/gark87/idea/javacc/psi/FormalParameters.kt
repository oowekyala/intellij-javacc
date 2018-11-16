package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author gark87
 */
class FormalParameters(node: ASTNode) : JavaccStub(node) {

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        val parameters = PsiTreeUtil.getChildrenOfType(this, FormalParameter::class.java)
                         ?: return true

        return parameters.all { it.processDeclarations(processor, state, lastParent, place) }
    }
}
