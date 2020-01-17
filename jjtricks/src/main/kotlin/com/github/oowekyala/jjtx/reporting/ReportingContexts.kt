package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.tasks.JjtxTaskKey

data class ReportingContext(
    val key: String,
    val suffix: String?
)

val RootContext = ReportingContext("", null)

val InitCtx = ReportingContext("init", null)

fun ReportingContext.subKey(suffix: String) = ReportingContext("$key:$suffix", null)

fun taskCtx(taskKey: JjtxTaskKey) = ReportingContext(taskKey.ref, null)
