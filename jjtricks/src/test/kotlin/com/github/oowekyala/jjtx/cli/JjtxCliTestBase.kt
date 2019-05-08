package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.util.*
import com.intellij.openapi.util.Comparing
import com.intellij.rt.execution.junit.FileComparisonFailure
import com.intellij.util.io.readText
import junit.framework.Assert.assertEquals
import org.apache.commons.io.FileUtils.copyDirectory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Files.createTempDirectory
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*


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
 * @author Clément Fournier
 */
abstract class JjtxCliTestBase {


    inner class TestBuilder {
        var expectedExitCode: ExitCode = ExitCode.OK

        /**
         * Directory to check in the tmp dir.
         */
        var outputRoot: String = "gen"

        /**
         * Set it to use the environment of another [JjtxCliTestBase].
         */
        var envOwner: Class<out JjtxCliTestBase> = this@JjtxCliTestBase.javaClass
    }

    private class MyTestCase(params: TestBuilder) {

        val tmpDir: Path = createTempDirectory(TmpPrefix)
        val resDir: Path = findTestDir(javaClass)
        val env: Path = findTestDir(params.envOwner).resolve("env").also { assert(it.isDirectory()) }
        val expectedStdout: Path? = resDir.resolve("stdout.txt").takeIf { it.exists() }
        val expectedStderr: Path? = resDir.resolve("stderr.txt").takeIf { it.exists() }
        val expectedOutput: Path? = resDir.resolve("expected").takeIf { it.isDirectory() }
        val actualOutput: Path = tmpDir.resolve(params.outputRoot)
        val expectedExitCode: ExitCode = params.expectedExitCode

        init {
            copyDirectory(env.toFile(), tmpDir.toFile())
        }

    }

    private data class StopError(override val message: String, val code: Int) : Error()


    fun doTest(vararg args: String, conf: TestBuilder.() -> Unit = {}) {

        val test = MyTestCase(TestBuilder().also(conf))

        val myStdout = ByteArrayOutputStream()
        val myStderr = ByteArrayOutputStream()

        val myIo = Io(
            wd = test.tmpDir,
            stdout = PrintStream(myStdout),
            stderr = PrintStream(myStderr),
            dateGetter = { Date(0) }, // invariant date
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
                val actualText =
                    // remove non-determinism by truncating tmp dir name
                    actual.toString(Charset.defaultCharset().name())
                        .replace(Regex("$TmpPrefix\\d+"), TmpPrefix)

                val expectedText = expectedFile.readText()
                if (!Comparing.equal(expectedText, actualText)) {
                    throw FileComparisonFailure("Text mismatch", expectedText, actualText, expectedFile.toString())
                }
            }

        }

        assertEquals(test.expectedStdout, myStdout)
        assertEquals(test.expectedStderr, myStderr)
        assertEquals(test.expectedExitCode, code)

        if (test.expectedOutput != null) {
            assertDirEquals(test.expectedOutput, test.actualOutput)
        }
    }


    companion object {

        private const val TmpPrefix = "jjtx-test"

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

