package com.github.oowekyala.jjtx.testutil

import com.github.oowekyala.ijcc.lang.util.AssertionMatcher
import com.github.oowekyala.jjtx.cli.JjtxCliTestBase
import com.github.oowekyala.jjtx.templates.vbeans.NodeVBean
import com.github.oowekyala.jjtx.templates.vbeans.NodeVBean.Companion.TreeLikeWitness
import com.github.oowekyala.jjtx.util.toPath
import com.github.oowekyala.treeutils.matchers.MatchingConfig
import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import com.github.oowekyala.treeutils.printers.KotlintestBeanTreePrinter
import io.kotlintest.shouldBe
import java.nio.file.Path

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

fun getCallerName(): String = getStackFrame(2).methodName
fun getThisMethodName(): String = getStackFrame(1).methodName

fun getStackFrame(int: Int): StackTraceElement = Thread.currentThread().stackTrace[int]
val SrcTestResources: Path = run {
    System.getProperty("jjtx.testEnv.jjtricks.testResDir")?.toPath()?.toAbsolutePath()
    // that's for when the tests are run inside the IDE
        ?: JjtxCliTestBase::class.java.protectionDomain.codeSource.location.file.toPath()
            .resolve("../../../src/test/resources").normalize()
}
