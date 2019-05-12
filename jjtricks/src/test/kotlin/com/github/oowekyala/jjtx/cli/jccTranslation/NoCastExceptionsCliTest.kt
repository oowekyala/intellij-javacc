package com.github.oowekyala.jjtx.cli.jccTranslation

import com.github.oowekyala.jjtx.cli.JjtxCliTestBase
import org.junit.Test

/**
 * @author Clément Fournier
 */
class NoCastExceptionsCliTest : JjtxCliTestBase() {

    @Test
    fun testNonJavaccOptionsAreErased() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )
}
