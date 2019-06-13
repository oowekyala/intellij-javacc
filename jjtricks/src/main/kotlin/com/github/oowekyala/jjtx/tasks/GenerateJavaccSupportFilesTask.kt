package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.templates.FileGenTask

class GenerateJavaccSupportFilesTask(taskCtx: TaskCtx) : GenerationTaskBase(taskCtx) {

    override val configString: String? = null
    override val exceptionCtx: String = "Generating JavaCC support files"

    override val generationTasks: Collection<FileGenTask> by lazy {
        ctx.jjtxOptsModel.javaccGen.supportFiles.values
    }

}
