package com.github.oowekyala.gark87.idea.javacc.psi

import com.intellij.lang.ASTNode
import java.util.*

/**
 * @author gark87
 */
class JavaccInput(node: ASTNode) : JavaccStub(node) {

    val productions: List<Production>
        get() {
            val result = LinkedList<Production>()
            for (child in children) {
                if (child is Production) {
                    result.add(child)
                }
            }
            return result
        }
}
