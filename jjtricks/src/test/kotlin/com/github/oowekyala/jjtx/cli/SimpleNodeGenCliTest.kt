package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class SimpleNodeGenCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleArg() = doTest("DummyExpr")

    @Test
    fun testOtherRoot() = doTest("DummyExpr.jjt", "-o", "flaba") {
        outputRoot = "flaba"
    }

}
