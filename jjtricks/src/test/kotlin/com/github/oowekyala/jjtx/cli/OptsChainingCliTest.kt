package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class OptsChainingCliTest : JjtxCliTestBase() {

    @Test
    fun testFullParentChaining() = doTest(
        "Java",
        "--dump-config",
        "--opts",
        "Java.jjtopts.yaml",
        "--opts",
        "./JavaParent.jjtopts.yaml"
    )

    @Test
    fun testShortnames() = doTest(
        "Java",
        "--dump-config",
        "--opts",
        "Java",
        "--opts",
        "./JavaParent"
    )


}
