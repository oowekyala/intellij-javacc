package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class NodeGenCliTests : JjtxCliTestBase() {


    @Test
    fun testMultiSourceNodeGen() = doTest("DummyExpr", "gen:nodes", "-s", "ignored")

    @Test
    fun testNoNodeGen() = doTest("DummyExpr", "gen:nodes", "--warn")

    @Test
    fun testSimpleNodeGen() = doTest("DummyExpr", "gen:nodes")

    @Test
    fun testOtherRoot() = doTest("DummyExpr.jjt", "gen:nodes", "-o", "flaba") {
        subpath = "simpleNodeGen"
        outputRoot = "flaba"
    }

    @Test
    fun testTemplateException() = doTest("DummyExpr", "gen:*")

    @Test
    fun testVisitorCompletion() = doTest("DummyExpr", "gen:visitors")


}
