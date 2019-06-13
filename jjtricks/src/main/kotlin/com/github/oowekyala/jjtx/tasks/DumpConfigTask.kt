package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.util.dataAst.toYaml

/**
 * Dumps the flattened configuration as a YAML file to stdout.
 */
class DumpConfigTask(private val ctx: TaskCtx) : JjtxTask() {

    override fun execute() {
        ctx.run {
            val opts = ctx.jjtxOptsModel as? OptsModelImpl ?: return // TODO report

            ctx.io.stdout.run {
                println("# Fully resolved JJTricks configuration")
                println("# Config file chain: ${ctx.chainDump}")
                println(opts.toYaml())
                flush()
            }
        }
    }
}
