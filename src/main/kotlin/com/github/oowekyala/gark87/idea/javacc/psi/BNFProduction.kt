package com.github.oowekyala.gark87.idea.javacc.psi

import com.github.oowekyala.gark87.idea.javacc.util.JavaCCIcons
import com.intellij.lang.ASTNode
import javax.swing.Icon

/**
 * @author gark87
 */
class BNFProduction(node: ASTNode) : NonTerminalProduction(node) {

    override val icon: Icon
        get() = JavaCCIcons.NONTERMINAL.icon

    override fun toString(): String = "JavaCC BNF Production: $text"
}
