package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.testutil.BaseTreeDumpTest
import com.github.oowekyala.jjtx.testutil.DataTreePrinter
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import org.junit.Test

/**
 * @author Clément Fournier
 */
class YamlParserTest : BaseTreeDumpTest<DataAstNode>(
    DataTreePrinter,
    "yamlSamples",
    ".yaml"
) {
    override fun parseFile(nis: NamedInputStream): DataAstNode = YamlLang.parse(nis)


    @Test
    fun `test sample 1`() = doTest("Sample1")

    @Test
    fun `test references no resolve 1`() = doTest("References")


    @Test
    fun `test references no resolve 2`() = doTest("References2")


}
