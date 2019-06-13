package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.reporting.debug
import com.github.oowekyala.jjtx.reporting.reportException
import com.github.oowekyala.jjtx.reporting.reportNormal
import com.github.oowekyala.jjtx.templates.FileGenTask
import com.github.oowekyala.jjtx.templates.Status
import org.apache.velocity.VelocityContext
import java.nio.file.Path

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
