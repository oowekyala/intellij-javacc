package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.tasks.JjtxTaskKey
import java.nio.file.Path

data class ReportingContext(
    val key: String,
    val suffix: String?
)

val RootContext = ReportingContext("", null)

val InitCtx = ReportingContext("init", null)

fun ReportingContext.subKey(suffix: String) = ReportingContext("$key:$suffix", null)
fun taskCtx(taskKey: JjtxTaskKey) = ReportingContext(taskKey.ref, null)
fun fileSubCtx(parent: ReportingContext, path: Path) = ReportingContext(parent.key, path.toString())
