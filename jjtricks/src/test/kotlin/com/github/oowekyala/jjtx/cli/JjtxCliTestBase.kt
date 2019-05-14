package com.github.oowekyala.jjtx.cli

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.testutil.SrcTestResources
import com.github.oowekyala.jjtx.testutil.getStackFrame
import com.github.oowekyala.jjtx.util.exists
import com.github.oowekyala.jjtx.util.io.ExitCode
import com.github.oowekyala.jjtx.util.io.Io
import com.github.oowekyala.jjtx.util.io.StringSource
import com.github.oowekyala.jjtx.util.io.TrailingSpacesFilterOutputStream
import com.github.oowekyala.jjtx.util.isDirectory
import com.github.oowekyala.jjtx.util.isFile
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


    inner class TestBuilder(var subpath: String) {
        var expectedExitCode: ExitCode = ExitCode.OK

        /**
         * Directory to check in the tmp dir.
         */
        var outputRoot: String = "gen"

        /**
         * Set it to use the environment of another [JjtxCliTestBase].
         */
        var envOwner: Class<out JjtxCliTestBase> = this@JjtxCliTestBase.javaClass


        var expectedErr: StringSource? = StringSource.File("stderr.txt")
        var expectedOut: StringSource? = StringSource.File("stdout.txt")

    }

    protected open fun mapEnv(default: Path): Path = default
    protected open fun mapRes(default: Path): Path = default

    private inner class MyTestCase(params: TestBuilder) {

        val tmpDir: Path = createTempDirectory(TmpPrefix)
        val resDir: Path = mapRes(findTestDir(this@JjtxCliTestBase.javaClass).resolve(params.subpath))

        val env: Path =
            findTestDir(params.envOwner)
                .resolve(params.subpath)
                .resolve("env")
                .let { mapEnv(it) }
                .also {
                    assert(it.isDirectory())
                }
        val expectedOutput: Path? = resDir.resolve("expected").takeIf { it.isDirectory() }

        val expectedStdout: StringSource? = params.expectedOut
        val expectedStderr: StringSource? = params.expectedErr

        val actualOutput: Path = tmpDir.resolve(params.outputRoot)
        val expectedExitCode: ExitCode = params.expectedExitCode

        init {
            copyDirectory(env.toFile(), tmpDir.toFile())
        }

    }

    private data class StopError(override val message: String, val code: Int) : Error()


    fun doTest(vararg args: String, conf: TestBuilder.() -> Unit = {}) {

        val callingMethod = getStackFrame(4).methodName.replace(Regex("(Cli)?[tT]ests?|\\d+"), "").decapitalize()
        val test = MyTestCase(TestBuilder(callingMethod).also(conf))

        val myStdout = ByteArrayOutputStream()
        val myStderr = ByteArrayOutputStream()

        val myIo = Io(
            wd = test.tmpDir,
            stdout = PrintStream(TrailingSpacesFilterOutputStream(myStdout)),
            stderr = PrintStream(TrailingSpacesFilterOutputStream(myStderr)),
            dateGetter = { Date(0) }, // invariant date
            exit = { m, code -> throw StopError(m, code) }
        )

        val code = try {
            Jjtricks.main(myIo, *args)
            ExitCode.OK
        } catch (stop: StopError) {
            ExitCode.values()[stop.code]
        }

        fun ByteArrayOutputStream.toActualText() =
            // remove non-determinism by truncating tmp dir name
            toString(Charset.defaultCharset().name())
                .replace(Regex("${Regex.escape(test.actualOutput.toString())}\\b"), "@output@")
                .replace(Regex("${Regex.escape(test.tmpDir.toString())}\\b"), "@tmp@")


        fun myAssertEquals(expectedSource: StringSource?,
                           actualText: String) {

            if (expectedSource != null) {


                val (fpath, expectedText) = when (expectedSource) {
                    is StringSource.File -> {
                        val p = test.resDir.resolve(expectedSource.fname).takeIf { it.isFile() }
                        Pair(p, p?.readText())
                    }
                    is StringSource.Str  -> Pair(null, expectedSource.source)
                }


                if (expectedText != null && !Comparing.equal(expectedText.trim(), actualText.trim())) {
                    throw FileComparisonFailure("Text mismatch", expectedText, actualText, fpath.toString())
                }
            }

        }

        // there's always the problem that testing one thing
        // before another might hide exceptions
        // Testing stderr first and failing might hide more grave
        // issues like files missing
        // We assert the directory, and if it fails we also print stderr
        // to show as much as possible
        System.err.println(myStderr.toActualText())

        if (test.expectedOutput != null) {
            try {
                assertDirEquals(test.expectedOutput, test.actualOutput)
            } catch (e: FileComparisonFailure) {
                throw e
            }
        }


        myAssertEquals(test.expectedStderr, myStderr.toActualText())
        myAssertEquals(test.expectedStdout, myStdout.toActualText())
        assertEquals(test.expectedExitCode, code)

    }


    companion object {

        private const val TmpPrefix = "jjtx-test"

        private fun findTestDir(javaClass: Class<*>): Path {

            val name = javaClass.simpleName.replace(Regex("(Cli)?Tests?|\\d+"), "").decapitalize()

            val path = javaClass.`package`.name.replace('.', '/')

            return SrcTestResources.resolve("$path/$name")
                .also { assert(it.isDirectory()) { "$it should have been a directory" } }
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

