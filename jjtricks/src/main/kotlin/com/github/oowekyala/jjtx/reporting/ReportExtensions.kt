package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Position
import java.util.*


fun MessageCollector.reportException(throwable: Throwable,
                                     contextStr: String? = null,
                                     altMessage: String? = null,
                                     fatal: Boolean = false,
                                     position: Position? = null) {
    reportEntry(
        ExceptionEntry(
            thrown = throwable,
            doFail = fatal,
            timeStamp = Date(),
            contextStr = contextStr,
            position = position,
            altMessage = altMessage
        )
    )
}

/**
 * Report a normal execution trace.
 */
fun MessageCollector.reportNormal(message: String) {
    report(message, MessageCategory.NORMAL_EXEC_MESSAGE)
}

/**
 * Report a non-fatal error, probably followed later by termination anyway.
 */
fun MessageCollector.reportNonFatal(message: String, position: Position?) {
    report(message, MessageCategory.NON_FATAL, position)
}

/**
 * Report a normal execution trace.
 */
fun MessageCollector.reportError(message: String, position: Position? = null): Nothing {
    val m = if (position == null) message else message + "\n" + position.toString()
    throw IllegalStateException(m)
}

fun MessageCollector.report(message: String,
                            category: MessageCategory,
                            vararg sourcePosition: Position?) {
    reportEntry(
        ReportEntry(
            category,
            message,
            category.minSeverity,
            listOfNotNull(*sourcePosition),
            Date()
        )
    )
}
