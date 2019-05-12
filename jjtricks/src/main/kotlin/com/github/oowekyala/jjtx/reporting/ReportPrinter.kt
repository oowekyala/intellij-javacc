package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.ijcc.util.indent
import com.github.oowekyala.jjtx.tasks.JjtxTaskKey
import java.io.PrintStream

/**
 * @author Clément Fournier
 */

interface ReportPrinter {

    fun withContext(contextStr: ReportingContext): ReportPrinter

    /**
     * Print the report of warnings + log messages collected.
     */
    fun onEnd()

    fun printEntry(reportEntry: ReportEntry)

    fun printEntry(reportEntry: ExceptionEntry)

}


object NoopReportPrinter : ReportPrinter {

    override fun withContext(contextStr: ReportingContext) = this

    override fun onEnd() {
        // do nothing
    }

    override fun printEntry(reportEntry: ReportEntry) {
        // do nothing
    }

    override fun printEntry(reportEntry: ExceptionEntry) {
        // do nothing
    }
}

/**
 * This is the default output printer.
 * Aggregates warnings, and hides exception stack traces.
 */
class AggregateReportPrinter private constructor(
    private val stream: PrintStream,
    private val collected: MutableList<ReportEntry>,
    private val contextStr: ReportingContext?
) : ReportPrinter {

    constructor(stream: PrintStream, contextStr: ReportingContext? = null) : this(stream, mutableListOf(), contextStr)

    private val padding = JjtxTaskKey.values().map { it.ref.length }.plus(InitCtx.displayName.length).max()!! + 4

    private var hadExceptions = false

    private val myErrorPrinter: ReportPrinter =
        FullReportPrinter(stream, indent = if (contextStr == null) "" else baseIndent)

    override fun withContext(contextStr: ReportingContext): ReportPrinter =
        AggregateReportPrinter(stream, collected, contextStr)

    override fun onEnd() {

        val bySeverity = collected.groupBy { it.severity }.withDefault { emptyList() }
        val warnings = bySeverity.getValue(Severity.WARNING)
        val errors = bySeverity.getValue(Severity.NON_FATAL) + bySeverity.getValue(Severity.FAIL)

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

    override fun printEntry(reportEntry: ReportEntry) {
        // only let normal messages get through
        if (reportEntry.severity == Severity.NORMAL) {
            iprintln(reportEntry.message)
        } else {
            collected += reportEntry
        }

        if (reportEntry.severity > Severity.NORMAL) {
            // an error
            myErrorPrinter.printEntry(reportEntry)
        }

    }


    private fun iprintln(string: String) {
        if (contextStr != null) {
            stream.print("[${contextStr.displayName}]".padEnd(padding))
        }
        stream.println(string)
    }

    private fun ExceptionEntry.printSingleException(numOccurred: Int) {
        val leader = if (numOccurred > 1) "$numOccurred exceptions" else "Exception"
        if (contextStr != null) {
            stream.println("$leader while ${contextStr.decapitalize()} (${thrown.javaClass.name})")
        } else {
            stream.println("$leader (${thrown.javaClass.name})")
        }

        if (position != null && altMessage != null) {
            stream.println(altMessage.indent(1))
            stream.println(position.toString().indent(1))
            stream.println()
        } else if (thrown.message != null) {
            stream.println(thrown.message!!.trim().indent(1))
            stream.println()
        }
    }

    override fun printEntry(reportEntry: ExceptionEntry) {
        reportEntry.printSingleException(1)
        hadExceptions = true

        if (reportEntry.doFail) {
            stream.println("Run with --warn to examine the stack trace")
            stream.flush()
            throw DoExitNowError()
        }
    }
}

private val baseIndent = "    "

class FullReportPrinter(private val stream: PrintStream,
                        private val contextStr: ReportingContext? = null,
                        private val indent: String = "") : ReportPrinter {

    init {
        if (contextStr != null) {
            stream.println("[${contextStr.displayName}]")
        }
    }

    private fun iprintln(string: String) {
        stream.print(indent)
        stream.println(string)
    }

    private val padding = Severity.values().map { it.displayName.length }.max()!! + 4

    /**
     * Doesn't maintain state so can return a new instance.
     */
    override fun withContext(contextStr: ReportingContext): ReportPrinter =
        FullReportPrinter(stream, contextStr, indent + baseIndent)


    override fun onEnd() {
        // do nothing
    }

    override fun printEntry(reportEntry: ReportEntry) {
        val (_, message, realSeverity, positions) = reportEntry
        val logLine = "[${realSeverity.displayName}]".padEnd(padding) + message
        iprintln(logLine)
        positions.forEach {
            iprintln(it.toString().indent(padding, indentStr = " "))
        }
    }

    override fun printEntry(reportEntry: ExceptionEntry) {
        with(reportEntry) {
            iprintln("Exception while $contextStr")
            reportEntry.thrown.printStackTrace(stream)
        }

        if (reportEntry.doFail) {
            stream.flush()
            throw DoExitNowError()
        }
    }
}
