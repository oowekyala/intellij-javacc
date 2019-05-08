package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.path
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.templates.FileGenTask
import com.github.oowekyala.jjtx.templates.RunVBean
import com.github.oowekyala.jjtx.templates.Status
import com.github.oowekyala.jjtx.templates.VisitorGenerationTask
import com.github.oowekyala.jjtx.util.toYaml
import com.github.oowekyala.jjtx.util.toYamlString
import org.apache.velocity.VelocityContext
import java.io.PrintStream
import java.nio.file.Path


sealed class JjtxTask {

    abstract fun execute()

}


/**
 * Dumps the flattened configuration as a YAML file to stdout.
 */
class DumpConfigTask(private val ctx: JjtxContext,
                     private val out: PrintStream) : JjtxTask() {

    override fun execute() {

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

abstract class GenerationTaskBase(
    val ctx: JjtxContext,
    val outputDir: Path,
    val otherSourceRoots: List<Path>
) : JjtxTask() {

    final override fun execute() {

        val tasks = generationTasks

        if (tasks.isEmpty()) return

        var generated = 0
        var aborted = 0
        var ex = 0

        val rootCtx = rootCtx()

        for (gen in tasks) {

            try {
                val (st, _, _) = gen.execute(
                    ctx,
                    rootCtx,
                    outputDir,
                    otherSourceRoots
                )

                when (st) {
                    Status.Aborted   -> aborted++
                    Status.Generated -> generated++
                }
            } catch (e: Exception) {
                ctx.messageCollector.reportException(e, exceptionCtx, fatal = false)
                ex++
            }
        }

        ctx.messageCollector.reportNormal("[$header]: $configString")
        if (generated > 0)
            ctx.messageCollector.reportNormal("Generated $generated classes in $outputDir")
        if (aborted > 0)
            ctx.messageCollector.reportNormal("$aborted classes were not generated because found in other output roots")
        if (ex > 0)
            ctx.messageCollector.reportNormal("$ex classes were not generated because of an exception")

    }

    protected abstract val generationTasks: List<FileGenTask>
    protected abstract val header: String
    protected abstract val configString: String
    protected abstract val exceptionCtx: String

    protected open fun rootCtx(): VelocityContext = ctx.globalVelocityContext
}

/**
 * Generate the visitors marked for execution in the opts file.
 */
class GenerateVisitorsTask(ctx: JjtxContext, outputDir: Path) :
    GenerationTaskBase(
        ctx,
        outputDir,
        emptyList()
    ) {


    override val header: String = "VISITOR_GEN"
    override val exceptionCtx: String = "Generating visitor"

    override val generationTasks: List<VisitorGenerationTask> by lazy {

        val (doExec, dont) = ctx.jjtxOptsModel.visitors.values.partition { it.execute }

        dont.forEach {
            ctx.messageCollector.report(
                "Visitor ${it.id} is not configured for execution",
                MessageCategory.VISITOR_NOT_RUN
            )
        }

        doExec
    }

    override val configString: String by lazy {
        generationTasks.joinToString { it.id }
    }


}

/**
 * Generate the visitors marked for execution in the opts file.
 */
class GenerateNodesTask(ctx: JjtxContext,
                        outputDir: Path,
                        otherSourceRoots: List<Path>,
                        private val activeIdOverride: String?) : GenerationTaskBase(ctx, outputDir, otherSourceRoots) {

    override val generationTasks: List<FileGenTask> by lazy {
        val activeId = activeIdOverride ?: ctx.jjtxOptsModel.activeNodeGenerationScheme
        val schemes = ctx.jjtxOptsModel.grammarGenerationSchemes

        if (activeId == null) {
            ctx.messageCollector.reportNormal("No node generation schemes configured")
            return@lazy emptyList<FileGenTask>()
        }

        val scheme = schemes[activeId] ?: run {
            ctx.messageCollector.reportNonFatal(
                "Node generation scheme '$activeId' not found, available ones are ${schemes.keys}",
                null
            )
            return@lazy emptyList<FileGenTask>()
        }

        scheme.templates.flatMap { it.toFileGenTasks() }
    }

    override fun rootCtx(): VelocityContext =
        VelocityContext(mapOf("run" to RunVBean.create(ctx)), super.rootCtx())


    override val header: String = "NODE_GEN"

    override val configString: String
        get() = activeIdOverride ?: ctx.jjtxOptsModel.activeNodeGenerationScheme ?: "(none)"

    override val exceptionCtx: String = "Generating nodes"
}
