package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class SimpleNodeGenCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleArg() = doTest("DummyExpr", "gen:nodes")

    @Test
    fun testOtherRoot() = doTest("DummyExpr.jjt", "gen:nodes", "-o", "flaba") {
        outputRoot = "flaba"
    }

}
