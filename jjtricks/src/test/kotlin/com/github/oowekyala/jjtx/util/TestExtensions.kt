package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.lang.util.AssertionMatcher
import com.github.oowekyala.jjtx.templates.NodeVBean
import com.github.oowekyala.jjtx.templates.NodeVBean.Companion.TreeLikeWitness
import com.github.oowekyala.treeutils.matchers.MatchingConfig
import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import com.github.oowekyala.treeutils.printers.KotlintestBeanTreePrinter
import io.kotlintest.shouldBe

/**
 * @author Clément Fournier
 */


typealias TypeHWrapper = TreeNodeWrapper<NodeVBean, NodeVBean>

typealias TypeHSpec = TypeHWrapper.() -> Unit

fun matchRoot(fqcn: String, nodeSpec: TypeHSpec)
    : AssertionMatcher<NodeVBean?> = {
    it.baseShouldMatchSubtree(
        MatchingConfig(
            adapter = TreeLikeWitness,
            errorPrinter = KotlintestBeanTreePrinter(TreeLikeWitness)
        ),
        false

    ) {
        this.it.klass.qualifiedName shouldBe fqcn
        nodeSpec()
    }
}

fun TypeHWrapper.node(fqcn: String, nodeSpec: TypeHSpec = EmptySpec) {
    child<NodeVBean> {
        this.it.klass.qualifiedName shouldBe fqcn
        nodeSpec()
    }
}

val EmptySpec: TypeHSpec = {}
