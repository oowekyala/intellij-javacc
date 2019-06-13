package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.ijcc.lang.model.parserPackage
import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.postprocessor.mapJavaccOutput
import com.github.oowekyala.jjtx.preprocessor.VanillaJjtreeBuilder
import com.github.oowekyala.jjtx.preprocessor.toJavacc
import com.github.oowekyala.jjtx.reporting.*
import com.github.oowekyala.jjtx.templates.FileGenTask
import com.github.oowekyala.jjtx.templates.Status
import com.github.oowekyala.jjtx.util.*
import com.github.oowekyala.jjtx.util.dataAst.toYaml
import com.github.oowekyala.jjtx.util.io.Io
import org.apache.velocity.VelocityContext
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import org.javacc.parser.Main as JavaccMain


enum class JjtxTaskKey(
    val ref: String,
    private val taskBuilder: (TaskCtx) -> JjtxTask,
    vararg val serialDependencies: JjtxTaskKey) {
    DUMP_CONFIG("help:dump-config", ::DumpConfigTask),
    GEN_COMMON("gen:common", ::CommonGenTask),
    GEN_NODES("gen:nodes", ::GenerateNodesTask),
    GEN_SUPPORT("gen:javacc-support", ::GenerateJavaccSupportFilesTask),
    GEN_JAVACC("gen:javacc", ::GenerateJavaccTask),
    GEN_PARSER("gen:parser", ::JavaccExecTask, GEN_JAVACC)
    ;



    val namespace = ref.substringBefore(':')
    val localName = ref.substringAfter(':')

    override fun toString(): String = ref


    fun execute(taskCtx: TaskCtx): CompletableFuture<Void?> {
        val task = taskBuilder(taskCtx)
        return when {
            !Jjtricks.TEST_MODE -> CompletableFuture.runAsync(task::execute)
            else                -> CompletableFuture.completedFuture(task.execute()).thenApply { null }
        }
    }


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
            .plus(JjtxOptsModel.RootJjtOpts.filename)
            // the "element =" here is not optional, since Path <: Iterable<Path>,
            // it could append all segments if not disambiguated
            .plus(element = io.wd.relativize(grammarFile.path))
            .joinToString(separator = " -> ")


data class TaskCtx(
    val ctx: JjtxContext,
    val outputDir: Path,
    val otherSourceRoots: List<Path>
)

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

abstract class GenerationTaskBase(taskCtx: TaskCtx) : JjtxTask() {

    protected val ctx: JjtxContext = taskCtx.ctx
    private val outputDir: Path = taskCtx.outputDir
    private val otherSourceRoots: List<Path> = taskCtx.otherSourceRoots

    final override fun execute() {

        val tasks = generationTasks

        if (tasks.isEmpty()) return

        var generated = 0
        var aborted = 0
        var ex = 0

        val rootCtx = rootCtx()

        configString?.let { ctx.messageCollector.reportNormal("Executing tasks '$it'") }

        tasks.let {
            // make it non-parallel to be reproducible for unit tests
            if (Jjtricks.TEST_MODE) it.stream() else it.parallelStream()
        }
            .forEach { gen ->
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

        fun reportNum(i: Int, message: (Pair<String, String>) -> String) {
            if (i > 0) {
                val withClass = if (i == 1) "1 class" to "was" else "$i classes" to "were"
                ctx.messageCollector.reportNormal(message(withClass))
            }
        }

        reportNum(generated) { (num, _) ->
            "Generated $num in $outputDir"
        }
        reportNum(aborted) { (num, verb) ->
            "$num $verb not generated because found in other source roots"
        }
        reportNum(ex) { (num, verb) ->
            "$num $verb not generated because of an exception"
        }
    }

    protected abstract val generationTasks: Collection<FileGenTask>
    protected abstract val configString: String?
    protected abstract val exceptionCtx: String

    protected open fun rootCtx(): VelocityContext = ctx.globalVelocityContext
}

/**
 * Generate the files that don't depend on any node bean.
 */
class CommonGenTask(taskCtx: TaskCtx) : GenerationTaskBase(taskCtx) {


    override val exceptionCtx: String = "Generating common files"

    override val generationTasks: Collection<FileGenTask> by lazy {
        ctx.jjtxOptsModel.commonGen.values
    }


    override val configString: String by lazy {
        ctx.jjtxOptsModel.commonGen.keys.joinToString()
    }


}

/**
 * Generate the visitors marked for execution in the opts file.
 */
class GenerateNodesTask(taskCtx: TaskCtx) : GenerationTaskBase(taskCtx) {

    override val generationTasks: List<FileGenTask> by lazy {
        ctx.jjtxOptsModel
            .nodeGen
            ?.templates
            ?.flatMap { it.toFileGenTasks(ctx) }
            ?: run {
                ctx.messageCollector.reportNormal("No node generation scheme found (set jjtx.nodeGen)")
                emptyList<FileGenTask>()
            }
    }

    override val configString: String? = null
    override val exceptionCtx: String = "Generating node files"
}

private fun JjtxContext.genJjPath(outputDir: Path) =
    outputDir.resolveQname(jjtxOptsModel.parserPackage).resolve("$grammarName.jj")

/**
 * Compile the grammar to a JavaCC file.
 */
class GenerateJavaccTask(private val taskCtx: TaskCtx) : JjtxTask() {


    override fun execute() {

        taskCtx.run {

            val invalidSyntax = ctx.grammarFile.reportSyntaxErrors(ctx)

            if (invalidSyntax) {
                return
            }

            val o = ctx.genJjPath(outputDir)

            if (!o.exists()) {
                o.createFile()
            }

            val opts = ctx.jjtxOptsModel.javaccGen

            try {

                val builder = VanillaJjtreeBuilder(ctx.grammarFile.grammarOptions, opts)

                FileOutputStream(o.toFile()).buffered()
                    .use {
                        toJavacc(ctx.grammarFile, it, opts, builder)
                    }

                ctx.messageCollector.reportNormal("Generated JavaCC grammar $o")

            } catch (ioe: IOException) {
                ctx.messageCollector.reportException(ioe, contextStr = "Generating JavaCC file", fatal = false)
            }
        }
    }
}

class GenerateJavaccSupportFilesTask(taskCtx: TaskCtx) : GenerationTaskBase(taskCtx) {

    override val configString: String? = null
    override val exceptionCtx: String = "Generating JavaCC support files"

    override val generationTasks: Collection<FileGenTask> by lazy {
        ctx.jjtxOptsModel.javaccGen.supportFiles.values
    }

}


class JavaccExecTask(private val ctx: TaskCtx) : JjtxTask() {


    override fun execute() {

        with(ctx) {
            val jj = ctx.genJjPath(outputDir)

            if (!jj.exists()) {
                ctx.messageCollector.reportNonFatal("JavaCC grammar not found: $jj\nCan't execute JavaCC")
                return
            }

            val tmpOutput = Files.createTempDirectory("javacc-from-jjtricks")

            val jccExitCode = synchronized(mutex) {
                try {
                    // TODO redirect IO

                    JavaccMain.mainProgram(
                        arrayOf(
                            "-OUTPUT_DIRECTORY=$tmpOutput",
                            "-STATIC=false",
                            jj.toString()
                        )
                    )


                } catch (t: Throwable) {
                    ctx.messageCollector.reportFatalException(t, "Executing JavaCC")
                }
            }


            if (jccExitCode == JAVACC_ERROR) {
                ctx.messageCollector.reportFatal("JavaCC exited with abnormal status code ($JAVACC_ERROR)")
            }

            ctx.messageCollector.report("Ran JavaCC in $tmpOutput", MessageCategory.DEBUG)

            mapJavaccOutput(
                ctx = ctx,
                jccOutput = tmpOutput,
                realOutput = outputDir,
                otherSources = otherSourceRoots
            )
        }

    }

    companion object {
        /** JavaCC is the least thread-safe program there is... */
        private val mutex = Object()
        private const val JAVACC_ERROR = 1
    }

}


