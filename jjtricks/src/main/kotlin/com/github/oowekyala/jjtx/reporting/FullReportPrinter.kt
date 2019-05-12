package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.ijcc.util.indent
import com.github.oowekyala.jjtx.util.baseIndent
import java.io.PrintStream


class FullReportPrinter(
    private val stream: PrintStream,
    private val minSeverity: Severity,
    private val contextStr: ReportingContext? = null,
    private val indent: String = ""
) : MessageCollector {

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
    override fun withContext(contextStr: ReportingContext): MessageCollector =
        FullReportPrinter(stream, minSeverity, contextStr, indent + baseIndent)


    override fun concludeReport() {
        // do nothing
    }

    override fun reportEntry(reportEntry: ReportEntry) {
        if (reportEntry.severity < minSeverity) return

        val (_, message, realSeverity, positions) = reportEntry
        val logLine = "[${realSeverity.displayName}]".padEnd(padding) + message
        iprintln(logLine)
        positions.forEach {
            iprintln(it.toString().indent(padding, indentStr = " "))
        }
    }

    override fun reportEntry(reportEntry: ExceptionEntry) {
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
