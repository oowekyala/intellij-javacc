package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author gark87
 */
class VariableDeclaratorId(node: ASTNode) : JavaccStub(node) {

    val identifier: Identifier?
        get() = PsiTreeUtil.getChildOfType(this, Identifier::class.java)
}
