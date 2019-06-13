package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.Jjtricks
import org.junit.Before
import org.junit.Test

/**
 * @author Clément Fournier
 */
class JccTranslationCliTests : JjtxCliTestBase(replaceExpected = true) {

    @Before
    fun before() {
        Jjtricks.TEST_MODE = true
    }

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
        "gen:javacc",
        "--warn"
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

    @Test
    fun testJjtThisNormalScopes() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testCommentBug() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

    @Test
    fun testDefaultVisibilityCompat() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )
    @Test
    fun testVisibilityOverride() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )

}
