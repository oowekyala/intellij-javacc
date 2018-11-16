package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * @author gark87
 */
class JavacodeProduction(node: ASTNode) : NonTerminalProduction(node) {

    override val icon: Icon by lazy { IconLoader.getIcon("/nodes/method.png") }

    override fun toString(): String = "JavaCC JAVACODE Production: $text"
}
