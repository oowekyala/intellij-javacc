package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.util.StringSource
import com.github.oowekyala.jjtx.util.ExitCode
import org.junit.Test

/**
 * @author Clément Fournier
 */
class WrongDirsCliTest : JjtxCliTestBase() {

    @Test
    fun testSourceIsFile() = doTest("DummyExpr", "-s", "iamfile") {
        expectedExitCode = ExitCode.ERROR
        expectedErr = StringSource.Str(
            "jjtricks: -s @tmp@/iamfile is not a directory"
        )
    }

    @Test
    fun testOutIsFile() = doTest("DummyExpr", "-o", "iamfile") {
        expectedExitCode = ExitCode.ERROR
        expectedErr = StringSource.ofTrimmed {
            "jjtricks: -o @tmp@/iamfile is not a directory"
        }
    }

}
