package com.github.oowekyala.jjtx.cli.jccTranslation

import com.github.oowekyala.jjtx.cli.JjtxCliTestBase
import org.junit.Test

/**
 * @author Clément Fournier
 */
class BetterNodeNamesCliTest : JjtxCliTestBase() {

    @Test
    fun testBetterNodeNames() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )
}
