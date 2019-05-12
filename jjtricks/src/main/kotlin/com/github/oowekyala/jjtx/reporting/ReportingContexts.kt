package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.tasks.JjtxTaskKey
import java.nio.file.Path

sealed class ReportingContext(
    val key: String,
    val suffix: String?
)

object InitCtx : ReportingContext("init", null)

data class TaskCtx(val taskKey: JjtxTaskKey) : ReportingContext(taskKey.ref, null)

data class FileSubCtx(val parent: ReportingContext, val path: Path) : ReportingContext(parent.key, path.toString())
