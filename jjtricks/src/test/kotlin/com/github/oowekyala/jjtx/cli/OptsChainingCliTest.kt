package com.github.oowekyala.jjtx.cli

/**
 * @author Clément Fournier
 */
class OptsChainingCliTest : JjtxCliTestBase() {

    init {
        // TODO allow referring to them as just JavaParent, Java
        args = listOf(
            "Java",
            "--dump-config",
            "--opts",
            "Java.jjtopts.yaml",
            "--opts",
            "JavaParent.jjtopts.yaml"
        )
    }

}
