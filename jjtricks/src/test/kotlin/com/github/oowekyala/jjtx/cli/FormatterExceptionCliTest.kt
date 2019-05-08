package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class FormatterExceptionCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleArg() = doTest("DummyExpr")

}
