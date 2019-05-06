package com.github.oowekyala.jjtx.cli.configDump

import com.github.oowekyala.jjtx.cli.JjtxCliTestBase

/**
 * @author Clément Fournier
 */
class ConfigDumpCliTest : JjtxCliTestBase() {

    init {
        args = listOf("Java", "--dump-config")
    }

}
