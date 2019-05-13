package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class JccTranslationCliTests : JjtxCliTestBase() {
    @Test
    fun testBetterNodeNames() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testImportInsteadOfImplements() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testNoCastExceptions() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testNodeFactoryCompat() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testSimpleTranslation() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testOptionErasure() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testMoreImplements() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testJjtThisScope() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

}
