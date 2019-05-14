package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.testutil.BaseTreeDumpTest
import com.github.oowekyala.jjtx.testutil.DataTreePrinter
import com.github.oowekyala.jjtx.util.io.namedInputStream
import org.junit.Test

/**
 * @author Clément Fournier
 */
class YamlParserTest : BaseTreeDumpTest<DataAstNode>(
    DataTreePrinter,
    "yamlSamples",
    ".yaml"
) {
    override fun parseFile(fileText: String): DataAstNode = YamlLang.parse(fileText.namedInputStream())


    @Test
    fun `test sample 1`() = doTest("Sample1")


}
