package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.tasks.JjtxTaskKey

sealed class ReportingContext(val displayName: String) {
    override fun toString(): String = displayName
}

object InitCtx : ReportingContext("init")

data class TaskCtx(val taskKey: JjtxTaskKey) : ReportingContext(taskKey.ref)
