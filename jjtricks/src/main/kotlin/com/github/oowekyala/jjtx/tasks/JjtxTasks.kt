package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.path
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.templates.Status
import com.github.oowekyala.jjtx.util.toYaml
import com.github.oowekyala.jjtx.util.toYamlString
import java.io.PrintStream
import java.nio.file.Path


sealed class JjtxTask {

    abstract fun execute(ctx: JjtxContext)

}


/**
 * Dumps the flattened configuration as a YAML file to stdout.
 */
data class DumpConfigTask(private val out: PrintStream) : JjtxTask() {

    override fun execute(ctx: JjtxContext) {

        val opts = ctx.jjtxOptsModel as? OptsModelImpl ?: return

        val chainDump =
            ctx.configChain
                .map { ctx.io.wd.relativize(it).normalize() }
                .plus("/jjtx/Root.jjtopts.yaml")
                // the "element =" here is not optional, since Path <: Iterable<Path>,
                // it could append all segments if not disambiguated
                .plus(element = ctx.io.wd.relativize(ctx.grammarFile.path))
                .joinToString(separator = " -> ", prefix = "Config file chain: ")

        out.println("# Fully resolved JJTricks configuration")
        out.println("# $chainDump")
        out.println(opts.toYaml().toYamlString())
        out.flush()
    }
}

/**
 * Generate the visitors marked for execution in the opts file.
 */
data class GenerateVisitorsTask(private val outputDir: Path) : JjtxTask() {

    override fun execute(ctx: JjtxContext) {

        val globalCtx = ctx.globalVelocityContext

        for ((id, visitor) in ctx.jjtxOptsModel.visitors) {

            if (!visitor.execute) {
                ctx.messageCollector.report(
                    "Visitor $id is not configured for execution",
                    MessageCategory.VISITOR_NOT_RUN
                )
                continue
            }

            try {
                val (_, _, o) = visitor.execute(ctx, globalCtx, outputDir, emptyList())
            } catch (e: Exception) {
                // FIXME report cleanly
                e.printStackTrace()
            }
        }
    }
}

/**
 * Generate the visitors marked for execution in the opts file.
 */
data class GenerateNodesTask(private val outputDir: Path,
                             private val otherSourceRoots: List<Path>) : JjtxTask() {

    override fun execute(ctx: JjtxContext) {

        val scheme = ctx.jjtxOptsModel.grammarGenerationScheme

        if (scheme == null) {
            ctx.messageCollector.reportNormal("No node generation schemes configured")
            return
        }

        var generated = 0
        var aborted = 0

        for (gen in scheme.templates.flatMap { it.toFileGenTasks() }) {

            val (st, _, _) = gen.execute(
                ctx,
                ctx.globalVelocityContext,
                outputDir,
                otherSourceRoots
            )

            when (st) {
                Status.Aborted   -> aborted++
                Status.Generated -> generated++
            }
        }

        ctx.messageCollector.reportNormal("Generated $generated classes in $outputDir")
        ctx.messageCollector.reportNormal("$aborted classes were not generated because found in other output roots")
    }
}
