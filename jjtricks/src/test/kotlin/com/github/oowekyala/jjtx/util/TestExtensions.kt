package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.lang.util.AssertionMatcher
import com.github.oowekyala.jjtx.typeHierarchy.TreeLikeWitness
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.treeutils.matchers.MatchingConfig
import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import com.github.oowekyala.treeutils.printers.KotlintestBeanTreePrinter
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 */


typealias TypeHWrapper = TreeNodeWrapper<TypeHierarchyTree, TypeHierarchyTree>

typealias TypeHSpec = TypeHWrapper.() -> Unit

fun matchRoot(fqcn: String, nodeSpec: TypeHSpec)
    : AssertionMatcher<TypeHierarchyTree?> = {
    it.baseShouldMatchSubtree(
        MatchingConfig(
            adapter = TreeLikeWitness,
            errorPrinter = KotlintestBeanTreePrinter(TreeLikeWitness)
        ),
        false

    ) {
        this.it.nodeName shouldBe fqcn
        nodeSpec()
    }
}

fun TypeHWrapper.node(fqcn: String, nodeSpec: TypeHSpec = EmptySpec): Unit {
    child<TypeHierarchyTree> {
        it.nodeName shouldBe fqcn
        nodeSpec()
    }
}

val EmptySpec: TypeHSpec = {}
