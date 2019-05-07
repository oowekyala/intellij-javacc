package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Position
import java.util.*

data class ReportEntry(
    val errorCategory: ErrorCategory,
    val message: String,
    val severity: Severity,
    val positions: List<Position>,
    val timeStamp: Date
)
