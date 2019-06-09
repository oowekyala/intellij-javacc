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
    private val exceptionMerger: ExceptionMerger,
    private val context: ReportingContext?
) : MessageCollector {

    constructor(stream: PrintStream, contextStr: ReportingContext? = null)
        : this(stream, mutableListOf(), ExceptionMerger(), contextStr)

    private val padding = JjtxTaskKey.values().map { it.ref.length }.plus("init".length).max()!! + 4

    private val myErrorPrinter =
        FullReportPrinter(
            stream = stream,
            minSeverity = Severity.IGNORE,
            printStackTrace = false,
            contextStr = context,
            indent = if (context == null) "" else baseIndent
        )

    override fun withContext(contextStr: ReportingContext): MessageCollector =
        // share same [collected]
        AggregateReportPrinter(
            stream = stream,
            collected = collected,
            exceptionMerger = exceptionMerger,
            context = contextStr
        )

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

            stream.println()
            stream.print("JJTricks exited with ")

            val str =
                if (e != null && w != null) "$e, and $w" else e ?: w!!

            stream.println(str)
            stream.println("Rerun with --warn option for more details")
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

            val thrown = reportEntry.thrown
            if (thrown != null && exceptionMerger.add(thrown, reportEntry.message) && reportEntry.positions.isNotEmpty()) {
                myErrorPrinter.printExceptionPosition(reportEntry.positions.first())
            } else {
                // an error
                myErrorPrinter.reportEntry(reportEntry)
            }
        }

    }


    private fun iprintln(string: String) {
        if (context != null) {
            stream.print("[${context.key}]".padEnd(padding))
        }
        stream.println(string)
    }
}
