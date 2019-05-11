package com.github.oowekyala.jjtx.cli.jccTranslation

import com.github.oowekyala.jjtx.cli.JjtxCliTestBase
import org.junit.Test

/**
 * @author Clément Fournier
 */
class JjtThisScopeCliTest : JjtxCliTestBase() {

    @Test
    fun testMoreImplements() = doTest(
        "SimpleExprs",
        "gen:javacc"
    )
}
