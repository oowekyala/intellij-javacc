package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.reporting.reportNormal
import com.github.oowekyala.jjtx.templates.FileGenTask

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
