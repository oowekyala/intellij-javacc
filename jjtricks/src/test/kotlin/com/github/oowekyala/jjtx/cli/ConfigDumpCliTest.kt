package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class ConfigDumpCliTest : JjtxCliTestBase() {

    @Test
    fun testSimpleArg() = doTest("DummyExpr", "help:dump-config")
    @Test
    fun testQual() = doTest("DummyExpr.jjt", "help:dump-config")
    @Test
    fun testPath() = doTest("././DummyExpr.jjt", "help:dump-config")
    @Test
    fun testWrongArgsArentChecked() = doTest("DummyExpr", "help:dump-config", "-o idontexist", "-s idedede")

}
