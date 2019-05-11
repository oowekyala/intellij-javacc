package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class NoNodeGenCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleArg() = doTest("DummyExpr", "gen:nodes")
    @Test
    fun testQual() = doTest("DummyExpr.jjt", "gen:nodes")

}
