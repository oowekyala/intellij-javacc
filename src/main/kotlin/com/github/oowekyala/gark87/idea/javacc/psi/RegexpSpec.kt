package com.github.oowekyala.gark87.idea.javacc.psi

import com.github.oowekyala.idea.javacc.util.JavaCCIcons
import com.intellij.lang.ASTNode
import javax.swing.Icon

/**
 * @author gark87
 */
class RegexpSpec(node: ASTNode) : JavaccStub(node), DeclarationForStructureView {

    override val identifier: Identifier?
        get() {
            val children = children
            if (children.isEmpty()) {
                return null
            }
            val regularExp = children[0]
            for (grandChild in regularExp.children) {
                if (grandChild.node.elementType === JavaCCTreeConstants.JJTIDENTIFIER) {
                    val result = grandChild.firstChild
                    return result as? Identifier
                }
            }
            return null
        }

    override val icon: Icon
        get() = JavaCCIcons.TERMINAL.icon
}
