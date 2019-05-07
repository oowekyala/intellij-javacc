package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.util.*
import com.intellij.openapi.util.Comparing
import com.intellij.rt.execution.junit.FileComparisonFailure
import com.intellij.util.io.readText
import junit.framework.Assert.assertEquals
import org.apache.commons.io.FileUtils.copyDirectory
import org.junit.Before
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


    protected var expectedExitCode = ExitCode.OK
    protected var expectedOutputRoot = "gen"

    private val myTmpDir = createTempDirectory("jjtx-test")
    private val myResourceDir = findTestDir(javaClass)

    private val myExpectedStdout = myResourceDir.resolve("stdout.txt").takeIf { it.exists() }
    private val myExpectedStderr = myResourceDir.resolve("stderr.txt").takeIf { it.exists() }
    private val myExpectedOutput = myResourceDir.resolve("expected").takeIf { it.isDirectory() }

    private val myStdout = ByteArrayOutputStream()
    private val myStderr = ByteArrayOutputStream()

    private data class StopError(override val message: String, val code: Int) : Error()

    @Before
    fun setup() {

        copyDirectory(myResourceDir.resolve("env").toFile(), myTmpDir.toFile())

    }

    fun doTest(vararg args: String) {

        val myIo = Io(
            wd = myTmpDir,
            stdout = PrintStream(myStdout),
            stderr = PrintStream(myStderr),
            exit = { m, code -> throw StopError(m, code) }
        )

        val code = try {
            Jjtricks.main(myIo, *args)
            ExitCode.OK
        } catch (stop: StopError) {
            ExitCode.values()[stop.code]
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
        assertEquals(expectedExitCode, code)

        if (myExpectedOutput != null) {
            assertDirEquals(myExpectedOutput, myTmpDir.resolve(expectedOutputRoot))
        }
    }


    companion object {

        private fun findTestDir(javaClass: Class<*>): Path {

            val name = javaClass.simpleName.removeSuffix("CliTest").decapitalize()

            val path = JjtxCliTestBase::class.java.`package`.name.replace('.', '/')

            return SrcTestResources.resolve("$path/$name").also { assert(it.isDirectory()) }
        }

        private val SrcTestResources = let {
            System.getProperty("jjtx.testEnv.jjtricks.testResDir")?.let { it.toPath().toAbsolutePath() }
                // that's for when the tests are run inside the IDE
                ?: JjtxCliTestBase::class.java.protectionDomain.codeSource.location.file.toPath()
                    .resolve("../../../src/test/resources").normalize()
        }

    }

}


private fun assertDirEquals(expected: Path, actual: Path) {

    /**
     * Returns the set of paths that exist in the [reference] and
     * can be found in the [inspected] directory under the same path.
     * Throws [FileComparisonFailure] if some files in [reference]
     * don't match their [inspected] counterpart.
     */
    fun walk(reference: Path, inspected: Path): Set<Path> {
        val found = mutableSetOf<Path>()

        if (!reference.exists()) {
            return emptySet()
        }

        Files.walkFileTree(reference, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val result = super.visitFile(file, attrs)

                // get the relative file name from path "one"
                val relativize = reference.relativize(file)
                // construct the path for the counterpart file in "other"
                val actualFile = inspected.resolve(relativize)

                val expectedText = file.readText()

                if (!actualFile.exists()) {
                    throw FileComparisonFailure(
                        "File $relativize missing in $inspected",
                        expectedText,
                        "",
                        actualFile.toString()
                    )
                }

                val actualText = actualFile.readText()

                if (!Comparing.equal(expectedText, actualText)) {
                    throw FileComparisonFailure(
                        "Text mismatch in file $actualFile",
                        expectedText,
                        actualText,
                        file.toString()
                    )
                }

                found.add(relativize)

                return result
            }
        })

        return found
    }

    val expectedInActual = walk(expected, actual)
    val actualInExpected = walk(actual, expected)
    assertEquals(expectedInActual, actualInExpected)
}

