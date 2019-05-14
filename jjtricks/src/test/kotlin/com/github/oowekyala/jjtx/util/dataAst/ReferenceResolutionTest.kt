package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.reporting.DoExitNowError
import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.testutil.BaseTreeDumpTest
import com.github.oowekyala.jjtx.testutil.DataTreePrinter
import com.github.oowekyala.jjtx.util.io.DefaultResourceResolver
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.github.oowekyala.jjtx.util.toPath
import io.kotlintest.matchers.types.shouldBeInstanceOf
import org.junit.Test

/**
 * @author Clément Fournier
 */
class ReferenceResolutionTest : BaseTreeDumpTest<DataAstNode>(
    DataTreePrinter,
    "resolve",
    ".yaml"
) {

    override fun parseFile(nis: NamedInputStream): DataAstNode =
        nis.parseAndResolveIncludes(DefaultResourceResolver(nis.filename.toPath().parent), MessageCollector.noop())

    @Test
    fun `test references simple resolve`() = doTest("OutRef")

    @Test
    fun `test self reference`() = doTest("SelfReference")

    @Test
    fun `test self inclusion`() = doNegTest("SelfReferenceNeg") {
        it.shouldBeInstanceOf<DoExitNowError>()
    }


}
