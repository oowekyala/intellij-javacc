package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author gark87
 */
class Block(node: ASTNode) : JavaccScope(node) {

    override fun processScopeDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        val declarations = PsiTreeUtil.getChildrenOfType(this, VariableDeclaration::class.java)
                           ?: return true

        return declarations.all { it.processDeclarations(processor, state, lastParent, place) }
    }
}
