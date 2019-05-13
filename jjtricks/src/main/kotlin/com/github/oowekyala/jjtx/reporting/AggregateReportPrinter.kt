package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.tasks.JjtxTaskKey
import com.github.oowekyala.jjtx.util.baseIndent
import java.io.PrintStream

/**
 * This is the default output printer.
 * Aggregates warnings, and hides exception stack traces.
 */
class AggregateReportPrinter private constructor(
    private val stream: PrintStream,
    private val collected: MutableList<ReportEntry>,
    private val context: ReportingContext?
) : MessageCollector {

    constructor(stream: PrintStream, contextStr: ReportingContext? = null) : this(stream, mutableListOf(), contextStr)

    private val padding = JjtxTaskKey.values().map { it.ref.length }.plus("init".length).max()!! + 4

    private var hadExceptions = false

    private val myErrorPrinter: MessageCollector =
        FullReportPrinter(
            stream = stream,
            minSeverity = Severity.IGNORE,
            printStackTrace = false,
            indent = if (context == null) "" else baseIndent
        )

    override fun withContext(contextStr: ReportingContext): MessageCollector =
        // share same [collected]
        AggregateReportPrinter(stream = stream, collected = collected, context = contextStr)

    override fun concludeReport() {

        val bySeverity = collected.groupBy { it.severity }.withDefault { emptyList() }
        val warnings = bySeverity.getValue(Severity.WARNING)
        val errors = bySeverity.getValue(Severity.NON_FATAL) + bySeverity.getValue(
            Severity.FAIL
        )

        if (warnings.isEmpty() && errors.isEmpty()) {
            return
        } else {

            val es = errors.size
            val e = if (es > 0) "$es error" + (if (es > 1) "s" else "") else null

            val ws = warnings.size
            val w = if (ws > 0) "$ws warning" + (if (ws > 1) "s" else "") else null

            stream.print("JJTricks exited with ")

            val str =
                if (e != null && w != null) "$e, and $w" else e ?: w!!

            stream.println(str)
            stream.println("Rerun with --warn option for more details")
        }

        if (hadExceptions) {
            stream.println("Rerun with --warn option to inspect the stack trace of exceptions")
        }

        stream.flush()
    }

    override fun reportEntry(reportEntry: ReportEntry) {
        // only let normal messages get through
        if (reportEntry.severity == Severity.NORMAL) {
            iprintln(reportEntry.message ?: return)
        } else {
            collected += reportEntry
        }

        if (reportEntry.severity > Severity.NORMAL) {
            // an error
            myErrorPrinter.reportEntry(reportEntry)
        }

    }


    private fun iprintln(string: String) {
        if (context != null) {
            stream.print("[${context.key}]".padEnd(padding))
        }
        stream.println(string)
    }
}
