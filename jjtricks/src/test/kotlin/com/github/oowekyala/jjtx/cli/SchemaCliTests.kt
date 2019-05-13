package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.testutil.getStackFrame
import com.github.oowekyala.jjtx.util.io.ExitCode
import com.github.oowekyala.jjtx.util.io.StringSource
import org.junit.Test
import java.nio.file.Path


class SchemaCliTests : JjtxCliTestBase() {

    override fun mapEnv(default: Path): Path = default.parent.parent
    override fun mapRes(default: Path): Path = default.parent

    private fun myTest(vararg args: String, conf: TestBuilder.() -> Unit = {}) {
        val testName = getStackFrame(4).methodName.capitalize()

        doTest("DummyExpr", "--opts", testName, *args) {
            expectedErr = StringSource.File("$testName.txt")
            expectedExitCode = ExitCode.ERROR
            conf()
        }
    }

    @Test
    fun multipleThRoots() = myTest()


}
