package com.github.oowekyala.jjtx.cli

/**
 * @author Clément Fournier
 */
class ConfigDumpCliTest : JjtxCliTestBase() {

    init {
        args = listOf("Java", "--dump-config")
    }

}
