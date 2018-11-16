package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author gark87
 */
abstract class NonTerminalProduction(node: ASTNode) : JavaccScope(node), DeclarationForStructureView {

    override val identifier: Identifier?
        get() {
            return when {
                children.size >= 3 -> children[2].firstChild as? Identifier
                else -> null
            }
        }

    override fun processScopeDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        val blocks = PsiTreeUtil.getChildrenOfType(this, Block::class.java)
        if (blocks != null && blocks.isNotEmpty()) {
            if (!blocks[0].processScopeDeclarations(processor, state, lastParent, place)) {
                return false
            }
        }
        val parameters = PsiTreeUtil.getChildrenOfType(this, FormalParameters::class.java)
        if (parameters != null) {
            for (params in parameters) {
                if (!params.processDeclarations(processor, state, lastParent, place)) {
                    return false
                }
            }
        }
        return true
    }
}
