package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class VisitorCompletionCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleVisitor() = doTest("Java")

}
