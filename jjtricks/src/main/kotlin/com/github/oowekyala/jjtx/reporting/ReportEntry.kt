package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Position
import java.util.*



data class ReportEntry(
    val message: String,
    val positions: List<Position>,
    val timeStamp: Date,
    val thrown: Throwable?,
    val messageCategory: MessageCategory?,
    val severity: Severity
)
