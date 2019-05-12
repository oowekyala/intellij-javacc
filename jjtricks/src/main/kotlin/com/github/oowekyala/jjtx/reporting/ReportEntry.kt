package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Position
import java.util.*

sealed class Reportable


data class ReportEntry(
    val messageCategory: MessageCategory,
    val message: String,
    val severity: Severity,
    val positions: List<Position>,
    val timeStamp: Date
) : Reportable()

data class ExceptionEntry(
    val thrown: Throwable,
    val doFail: Boolean,
    val timeStamp: Date,
    val contextStr: String?,
    val altMessage: String? ,
    val position: Position?
) : Reportable()
