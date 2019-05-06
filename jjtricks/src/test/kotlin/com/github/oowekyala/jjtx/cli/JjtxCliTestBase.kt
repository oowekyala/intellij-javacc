package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.util.ExitCode
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.exists
import com.github.oowekyala.jjtx.util.isDirectory
import com.intellij.openapi.util.Comparing
import com.intellij.rt.execution.junit.FileComparisonFailure
import com.intellij.util.io.readText
import junit.framework.Assert.assertEquals
import org.apache.commons.io.FileUtils.copyDirectory
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.*
import java.nio.file.Files.createTempDirectory
import java.nio.file.attribute.BasicFileAttributes


/**
 * Dir structure:
 *
 * In resource directory:
 *
 * - env/             working directory of the CLI run
 * - expected/        expected structure & contents of the output root after the run
 * - stdout.txt
 * - stderr.txt
 *
 *
 *
 *
 * @author Clément Fournier
 */
abstract class JjtxCliTestBase {


    protected var args = listOf<String>()
    protected var expectedExitCode = ExitCode.OK
    protected var expectedOutputRoot = "gen"

    private val myTmpDir = createTempDirectory("jjtx-test")
    private val myResourceDir = SrcTestResources.resolve(javaClass.`package`.name.replace('.', '/'))

    private val myExpectedStdout = myResourceDir.resolve("stdout.txt").takeIf { it.exists() }
    private val myExpectedStderr = myResourceDir.resolve("stderr.txt").takeIf { it.exists() }
    private val myExpectedOutput = myResourceDir.resolve("expected").takeIf { it.isDirectory() }

    private val myStdout = ByteArrayOutputStream()
    private val myStderr = ByteArrayOutputStream()

    private data class StopError(val code: Int) : Error()

    @Before
    fun setup() {

        copyDirectory(myResourceDir.resolve("env").toFile(), myTmpDir.toFile())

    }

    @Test
    fun doTest() {

        val myIo = Io(
            wd = myTmpDir,
            stdout = PrintStream(myStdout),
            stderr = PrintStream(myStderr),
            exit = { _, code -> throw StopError(code) }
        )

        try {
            Jjtricks.main(myIo, *args.toTypedArray())
        } catch (stop: StopError) {
            assertEquals(expectedExitCode, ExitCode.values()[stop.code])
        }

        fun assertEquals(expectedFile: Path?,
                         actual: ByteArrayOutputStream) {

            if (expectedFile != null) {
                val actualText = actual.toString(Charset.defaultCharset().name())
                val expectedText = expectedFile.readText()
                if (!Comparing.equal(expectedText, actualText)) {
                    throw FileComparisonFailure("Text mismatch", expectedText, actualText, expectedFile.toString())
                }
            }

        }

        assertEquals(myExpectedStdout, myStdout)
        assertEquals(myExpectedStderr, myStderr)

        if (myExpectedOutput != null) {
            assertDirEquals(myExpectedOutput, myTmpDir.resolve(expectedOutputRoot))
        }
    }


    companion object {

        private val SrcTestResources =
            Paths.get(System.getProperty("jjtx.testEnv.jjtricks.testResDir")).toAbsolutePath()

        private fun assertDirEquals(expected: Path, actual: Path) {
            Files.walkFileTree(expected, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class)
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val result = super.visitFile(file, attrs)

                    // get the relative file name from path "one"
                    val relativize = expected.relativize(file)
                    // construct the path for the counterpart file in "other"
                    val actualFile = actual.resolve(relativize)


                    val actualText = actualFile.readText()
                    val expectedText = file.readText()

                    if (!Comparing.equal(expectedText, actualText)) {
                        throw FileComparisonFailure(
                            "Text mismatch in file $actualFile",
                            expectedText,
                            actualText,
                            file.toString()
                        )
                    }

                    return result
                }
            })
        }
    }

}
