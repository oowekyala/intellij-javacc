package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.ijcc.lang.model.parserPackage
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.reporting.reportSyntaxErrors
import com.github.oowekyala.jjtx.preprocessor.toJavacc
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.templates.FileGenTask
import com.github.oowekyala.jjtx.templates.RunVBean
import com.github.oowekyala.jjtx.templates.Status
import com.github.oowekyala.jjtx.templates.VisitorGenerationTask
import com.github.oowekyala.jjtx.util.*
import org.apache.velocity.VelocityContext
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Path


enum class JjtxTaskKey(val ref: String) {
    DUMP_CONFIG("help:dump-config"),
    GEN_VISITORS("gen:visitors"),
    GEN_NODES("gen:nodes"),
    GEN_JAVACC("gen:javacc");


    val namespace = ref.substringBefore(':')
    val localName = ref.substringAfter(':')

    override fun toString(): String = ref

    companion object {
        private val nss = mutableMapOf<String, MutableList<String>>()
        private val refs = mutableMapOf<String, JjtxTaskKey>()

        init {
            for (k in values()) {
                nss.computeIfAbsent(k.namespace) { mutableListOf() } += k.localName
                refs[k.ref] = k
            }
        }

        fun parse(string: String, io: Io): List<JjtxTaskKey> {
            if (string in refs) return listOf(refs[string]!!)

            val (ns, local) = string.splitAroundFirst(':')

            return when {
                // foo:*
                local == "*" && ns.isNotEmpty() -> {
                    val nskeys = nss.getOrDefault(ns, emptyList<String>())
                    if (nskeys.isEmpty()) {
                        io.bail("Task group $ns not known, available tasks ${values().toSet()}")
                    }

                    nskeys.mapNotNull { refs["$ns:$it"] }
                }
                // foo is local
                ns.isEmpty()                    ->
                    refs.values.firstOrNull { it.localName == local }?.let { listOf(it) }
                        ?: io.bail("Task $local not known, available tasks ${values().toSet()}")
                else                            -> emptyList()
            }

        }

    }
}


sealed class JjtxTask {

    abstract fun execute()

}


val JjtxContext.chainDump
    get() =
        configChain
            .map { io.wd.relativize(it).normalize() }
            .plus("/jjtx/Root.jjtopts.yaml")
            // the "element =" here is not optional, since Path <: Iterable<Path>,
            // it could append all segments if not disambiguated
            .plus(element = io.wd.relativize(grammarFile.path))
            .joinToString(separator = " -> ")


/**
 * Dumps the flattened configuration as a YAML file to stdout.
 */
class DumpConfigTask(private val ctx: JjtxContext,
                     private val out: PrintStream) : JjtxTask() {

    override fun execute() {

        val opts = ctx.jjtxOptsModel as? OptsModelImpl ?: return


        out.println("# Fully resolved JJTricks configuration")
        out.println("# Config file chain: ${ctx.chainDump}")
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

        ctx.messageCollector.reportNormal("Executing profiles '$configString'")
        if (generated > 0)
            ctx.messageCollector.reportNormal("Generated $generated classes in $outputDir")
        if (aborted > 0)
            ctx.messageCollector.reportNormal("$aborted classes were not generated because found in other output roots")
        if (ex > 0)
            ctx.messageCollector.reportNormal("$ex classes were not generated because of an exception")

    }

    protected abstract val generationTasks: List<FileGenTask>
    protected abstract val configString: String
    protected abstract val exceptionCtx: String

    protected open fun rootCtx(): VelocityContext = ctx.globalVelocityContext
}

/**
 * Generate the visitors marked for execution in the opts file.
 */
class GenerateVisitorsTask(ctx: JjtxContext, outputDir: Path, sourceRoots: List<Path>)
    : GenerationTaskBase(ctx, outputDir, sourceRoots) {


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
                        otherSourceRoots: List<Path>) : GenerationTaskBase(ctx, outputDir, otherSourceRoots) {

    override val generationTasks: List<FileGenTask> by lazy {
        val activeId = ctx.jjtxOptsModel.activeNodeGenerationScheme
        val schemes = ctx.jjtxOptsModel.grammarGenerationSchemes

        if (activeId == null) {
            ctx.messageCollector.reportNormal("No node generation schemes configured (set jjtx.activeGenScheme)")
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


    override val configString: String
        get() = ctx.jjtxOptsModel.activeNodeGenerationScheme ?: "(none)"

    override val exceptionCtx: String = "Generating nodes"
}


/**
 * Generate the visitors marked for execution in the opts file.
 */
class GenerateJavaccTask(val ctx: JjtxContext,
                         private val outputDir: Path) : JjtxTask() {


    override fun execute() {


        val invalidSyntax = ctx.grammarFile.reportSyntaxErrors(ctx)

        if (invalidSyntax) {
            return
        }

        val o = outputDir.resolve(ctx.jjtxOptsModel.parserPackage.replace('.', '/')).resolve(ctx.grammarName + ".jj")

        if (!o.exists()) {
            o.createFile()
        }

        val opts = ctx.jjtxOptsModel.javaccGen

        try {

            FileOutputStream(o.toFile()).buffered()
                .use {
                    toJavacc(ctx.grammarFile, it, opts)
                }

            ctx.messageCollector.reportNormal("Generated JavaCC grammar $o")

        } catch (ioe: IOException) {
            ctx.messageCollector.reportException(ioe, contextStr = "Generating JavaCC file", fatal = false)
        }
    }
}
