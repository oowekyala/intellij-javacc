package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.Jjtricks
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
    private val printLock: Any = stream

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
        myErrorPrinter.concludeReport()

        val bySeverity = collected.groupBy { it.severity }.withDefault { emptyList() }
        val warnings = bySeverity.getValue(Severity.WARNING)
        val errors = bySeverity.getValue(Severity.NON_FATAL)
        val fatal = bySeverity.getValue(Severity.FAIL)

        if (fatal.isNotEmpty()) {
            stream.println()
            stream.println("JJTricks exited with a fatal error")
            stream.println("Rerun with --warn option for a stack trace")
            stream.println("If you think this is a bug, please report it to ${Jjtricks.GITHUB_URL}/issues/new")
        } else if (warnings.isEmpty() && errors.isEmpty()) {
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
            stream.println("Rerun with --warn option for more details" + (if (es > 0) " (e.g. stack traces)" else ""))
        }

        stream.flush()
    }


    override fun reportEntry(reportEntry: ReportEntry) {
        synchronized(printLock) {
            // only let normal messages get through
            if (reportEntry.severity == Severity.NORMAL) {
                iprintln(reportEntry.message?.trim() ?: return)
            } else {
                collected += reportEntry
            }

            if (reportEntry.severity > Severity.NORMAL) {

                val thrown = reportEntry.thrown
                if (
                    thrown != null
                    && exceptionMerger.add(thrown, reportEntry.message)
                    && reportEntry.positions.isNotEmpty()
                ) {
                    myErrorPrinter.printExceptionPosition(reportEntry.positions.first())
                } else {
                    // an error
                    stream.println()
                    myErrorPrinter.reportEntry(reportEntry)
                }
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
