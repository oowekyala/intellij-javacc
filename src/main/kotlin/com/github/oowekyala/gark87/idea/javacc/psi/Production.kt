package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author gark87
 */
class Production(node: ASTNode) : JavaccStub(node) {

    val nonTerminalProduction: NonTerminalProduction?
        get() = PsiTreeUtil.getChildOfType(this, NonTerminalProduction::class.java)

    val regexpProduction: RegexpProduction?
        get() = PsiTreeUtil.getChildOfType(this, RegexpProduction::class.java)

    override fun toString(): String = "JavaCC Production: $text"
}
