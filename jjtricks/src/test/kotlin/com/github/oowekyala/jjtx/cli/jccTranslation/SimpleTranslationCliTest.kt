package com.github.oowekyala.jjtx.cli.jccTranslation

import com.github.oowekyala.jjtx.cli.JjtxCliTestBase
import org.junit.Test

/**
 * @author Clément Fournier
 */
class SimpleTranslationCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleTranslation() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )
}
