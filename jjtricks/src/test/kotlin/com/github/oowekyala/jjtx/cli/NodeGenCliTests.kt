package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.Jjtricks
import org.junit.Before
import org.junit.Test

/**
 * @author Clément Fournier
 */
class NodeGenCliTests : JjtxCliTestBase(replaceExpected = false) {

    @Before
    fun before() {
        Jjtricks.TEST_MODE = true
    }

    @Test
    fun testMultiSourceNodeGen() = doTest("DummyExpr", "gen:nodes", "gen:common", "-s", "ignored")

    @Test
    fun testNoNodeGen() = doTest("DummyExpr", "gen:nodes", "--warn")

    @Test
    fun testSimpleNodeGen() = doTest("DummyExpr", "gen:nodes", "gen:common")

    @Test
    fun testOtherRoot() = doTest("DummyExpr.jjt", "gen:nodes", "gen:common", "-o", "flaba") {
        subpath = "simpleNodeGen"
        outputRoot = "flaba"
    }

    @Test
    fun testTemplateException() = doTest("DummyExpr", "gen:*")

    @Test
    fun testVisitorCompletion() = doTest("DummyExpr", "gen:common")

    @Test
    fun testGenericVisitor() = doTest("DummyExpr", "gen:common", "gen:nodes")

    @Test
    fun testGenericRecursiveVisitor() = doTest("DummyExpr", "gen:common")

    @Test
    fun testGenericNonDefaultVisitor() = doTest("DummyExpr", "gen:common")


}
