package com.github.oowekyala.jjtx.cli

import org.junit.Test

/**
 * @author Clément Fournier
 */
class OptsChainingCliTest : JjtxCliTestBase() {

    // TODO allow referring to them as just JavaParent, Java
    @Test
    fun testFullParentChaining() = doTest(
        "Java",
        "--dump-config",
        "--opts",
        "Java.jjtopts.yaml",
        "--opts",
        "JavaParent.jjtopts.yaml"
    )


}
