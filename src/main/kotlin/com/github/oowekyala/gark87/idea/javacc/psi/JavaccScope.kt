package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author gark87
 */
abstract class JavaccScope(node: ASTNode) : JavaccStub(node) {

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState,
                                     lastParent: PsiElement?, place: PsiElement): Boolean {
        if (!processScopeDeclarations(processor, state, lastParent, place)) {
            return false
        }
        val nextScope = findNextScope()
                        ?: return containingFile.processDeclarations(processor, state, this, place)
        return nextScope.processDeclarations(processor, state, this, place)
    }

    protected fun findNextScope(): JavaccScope? = PsiTreeUtil.getParentOfType(this, JavaccScope::class.java)

    abstract fun processScopeDeclarations(processor: PsiScopeProcessor,
                                          state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean
}
