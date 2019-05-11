package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class JavaccSimpleTranslationCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleTranslation() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )
}
