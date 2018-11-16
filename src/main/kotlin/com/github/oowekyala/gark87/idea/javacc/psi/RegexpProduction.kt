package com.github.oowekyala.gark87.idea.javacc.psi

import com.github.oowekyala.gark87.idea.javacc.util.filterByType
import com.intellij.lang.ASTNode

/**
 * @author gark87
 */
class RegexpProduction(node: ASTNode) : JavaccStub(node) {

    val allRegExpSpec: List<RegexpSpec>
        get() = children.filterByType()
}
