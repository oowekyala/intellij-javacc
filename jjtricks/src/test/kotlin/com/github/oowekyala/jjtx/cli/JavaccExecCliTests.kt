package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.Jjtricks
import org.junit.Before
import org.junit.Test

/**
 * @author Clément Fournier
 */
class JavaccExecCliTests : JjtxCliTestBase() {

    @Before
    fun before() {
        Jjtricks.TEST_MODE = true
    }

    @Test
    fun testNoPostProcessing() = doTest(
        "SimpleExprs",
        "gen:parser"
    )


}
