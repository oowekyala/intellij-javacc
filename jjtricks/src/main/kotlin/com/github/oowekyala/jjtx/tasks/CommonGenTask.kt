package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.templates.FileGenTask

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
