package com.github.oowekyala.jjtx.testutil

import com.github.oowekyala.treeutils.printers.TreePrinter
import com.intellij.rt.execution.junit.FileComparisonFailure
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals

/**
 * @author Clément Fournier
 */

/**
 * Compare a dump of a file against a saved baseline.
 *
 * @param printer The node printer used to dump the trees
 * @param pathToFixtures Path to the test files within the directory of the test case
 * @param extension Extension that the unparsed source file is supposed to have
 */
abstract class BaseTreeDumpTest<H : Any>(
    val printer: TreePrinter<H>,
    val pathToFixtures: String,
    val extension: String
) {

    /**
     * Parses the given string into a node.
     */
    abstract fun parseFile(fileText: String): H


    /**
     * Executes the test. The test files are looked up in [pathToFixtures],
     * in the resource directory *of the subclass*.
     * The reference test file must be named [fileBaseName] + [ExpectedExt].
     * The source file to parse must be named [fileBaseName] + [extension].
     */
    fun doTest(fileBaseName: String) {
        val expectedFile = findTestFile(javaClass, "$pathToFixtures/$fileBaseName$ExpectedExt").toFile()
        val sourceFile = findTestFile(javaClass, "$pathToFixtures/$fileBaseName$extension").toFile()

        assert(sourceFile.isFile) {
            "Source file $sourceFile is missing"
        }

        val parsed = parseFile(sourceFile.readText()) // UTF-8
        val actual = printer.dumpSubtree(parsed)

        if (!expectedFile.exists()) {
            expectedFile.writeText(actual)
            throw AssertionError("Reference file $expectedFile doesn't exist, created it anyway")
        }

        val expected = expectedFile.readText()

        if (expected != actual) {
            throw FileComparisonFailure(
                "Tree dump comparison failed, see the reference: $expectedFile",
                expected,
                actual,
                expectedFile.toPath().toAbsolutePath().toString()
            )
        }
    }

    private fun findTestFile(contextClass: Class<*>, resourcePath: String): Path {
        val path = contextClass.`package`.name.replace('.', '/')
        return SrcTestResources.resolve("$path/$resourcePath")
    }

    companion object {
        const val ExpectedExt = ".txt"
    }

}
