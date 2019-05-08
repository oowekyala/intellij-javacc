package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.lang.util.AssertionMatcher
import com.github.oowekyala.jjtx.templates.NodeBean
import com.github.oowekyala.jjtx.templates.NodeBean.Companion.TreeLikeWitness
import com.github.oowekyala.treeutils.matchers.MatchingConfig
import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import com.github.oowekyala.treeutils.printers.KotlintestBeanTreePrinter
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 */


typealias TypeHWrapper = TreeNodeWrapper<NodeBean, NodeBean>

typealias TypeHSpec = TypeHWrapper.() -> Unit

fun matchRoot(fqcn: String, nodeSpec: TypeHSpec)
    : AssertionMatcher<NodeBean?> = {
    it.baseShouldMatchSubtree(
        MatchingConfig(
            adapter = TreeLikeWitness,
            errorPrinter = KotlintestBeanTreePrinter(TreeLikeWitness)
        ),
        false

    ) {
        this.it.classQualifiedName shouldBe fqcn
        nodeSpec()
    }
}

fun TypeHWrapper.node(fqcn: String, nodeSpec: TypeHSpec = EmptySpec) {
    child<NodeBean> {
        it.classQualifiedName shouldBe fqcn
        nodeSpec()
    }
}

val EmptySpec: TypeHSpec = {}
