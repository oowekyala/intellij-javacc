package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class ConfigDumpCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleArg() = doTest("Java", "--dump-config")
    @Test
    fun testQual() = doTest("Java.jjt", "--dump-config")
    @Test
    fun testPath() = doTest("././Java.jjt", "--dump-config")

}
