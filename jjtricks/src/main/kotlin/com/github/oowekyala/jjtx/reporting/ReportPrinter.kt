package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.ijcc.util.indent
import java.io.PrintStream

/**
 * @author Clément Fournier
 */

interface ReportPrinter {

    /**
     * Print the report of warnings + log messages collected.
     */
    fun onEnd()

    fun printEntry(reportEntry: ReportEntry)

}


object NoopReportPrinter : ReportPrinter {
    override fun onEnd() {
        // do nothing
    }

    override fun printEntry(reportEntry: ReportEntry) {
        // do nothing
    }
}

class AggregateReportPrinter(private val stream: PrintStream) : ReportPrinter {

    private val collected = mutableListOf<ReportEntry>()

    override fun onEnd() {

        val warnings = collected.filter { it.severity == Severity.WARNING }

        if (warnings.isEmpty()) {
            return
        } else {
            val sizes = warnings.size
            stream.println("There are $sizes JJTricks warnings")
            stream.println("Rerun with --warn option to examine them")
        }
        stream.flush()
    }

    override fun printEntry(reportEntry: ReportEntry) {
        // only let normal messages get through
        if (reportEntry.severity == Severity.NORMAL) {
            stream.println(reportEntry.message)
        } else {
            collected += reportEntry
        }
    }
}

class FullReportPrinter(private val stream: PrintStream) : ReportPrinter {

    private val padding = Severity.values().map { it.name.length }.max()!! + 4

    override fun onEnd() {
        // do nothing
    }

    override fun printEntry(reportEntry: ReportEntry) {
        val (_, message, realSeverity, positions) = reportEntry
        val logLine = "[$realSeverity]".padEnd(padding) + message
        stream.println(logLine)
        positions.forEach {
            stream.println(it.toString().indent(padding, indentStr = " "))
        }
    }
}
