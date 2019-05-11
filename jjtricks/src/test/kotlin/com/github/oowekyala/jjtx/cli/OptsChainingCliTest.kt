package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class OptsChainingCliTest : JjtxCliTestBase() {

    @Test
    fun testFullParentChaining() = doTest(
        "DummyExpr",
        "help:dump-config",
        "--opts",
        "DummyExpr.jjtopts.yaml",
        "--opts",
        "./DummyExprParent.jjtopts.yaml"
    )

    @Test
    fun testShortnames() = doTest(
        "DummyExpr",
        "help:dump-config",
        "--opts",
        "DummyExpr",
        "--opts",
        "./DummyExprParent"
    )


}
