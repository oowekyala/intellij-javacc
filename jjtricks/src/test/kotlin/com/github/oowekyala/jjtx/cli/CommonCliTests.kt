package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.util.io.ExitCode
import com.github.oowekyala.jjtx.util.io.StringSource
import org.junit.Test

/**
 * @author Clément Fournier
 */
class CommonCliTests : JjtxCliTestBase() {


    @Test
    fun formatterException() = doTest("DummyExpr")

    @Test
    fun testUnparsableOpts() = doTest("DummyExpr") {
        expectedExitCode = ExitCode.ERROR
    }


    @Test
    fun testSourceIsFile() = doTest("DummyExpr", "-s", "iamfile") {
        subpath = "wrongDirs"
        expectedExitCode = ExitCode.ERROR
        expectedErr = StringSource.Str(
            "jjtricks: -s @tmp@/iamfile is not a directory"
        )
    }

    @Test
    fun testOutIsFile() = doTest("DummyExpr", "-o", "iamfile") {
        subpath = "wrongDirs"
        expectedExitCode = ExitCode.ERROR
        expectedErr = StringSource.ofTrimmed {
            "jjtricks: -o @tmp@/iamfile is not a directory"
        }
    }


    @Test
    fun testConfigDump1() = doTest("DummyExpr", "help:dump-config")

    @Test
    fun testConfigDump2() = doTest("DummyExpr.jjt", "help:dump-config")

    @Test
    fun testConfigDump3() = doTest("././DummyExpr.jjt", "help:dump-config")

    @Test
    fun testWrongArgsArentChecked() = doTest("DummyExpr", "help:dump-config", "-o idontexist", "-s idedede") {
        subpath = "configDump"
    }


    @Test
    fun testFullParentChaining() = doTest(
        "DummyExpr",
        "help:dump-config",
        "--opts",
        "DummyExpr.jjtopts.yaml",
        "--opts",
        "./DummyExprParent.jjtopts.yaml"
    ) {
        subpath = "optsChaining"
    }

    @Test
    fun testShortnames() = doTest(
        "DummyExpr",
        "help:dump-config",
        "--opts",
        "DummyExpr",
        "--opts",
        "./DummyExprParent"
    ) {
        subpath = "optsChaining"
    }

}
