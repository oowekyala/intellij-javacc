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

    private var contextPrinted = contextStr == null


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

        if (!contextPrinted && contextStr != null) {
            printContextHeader(contextStr)
        }

        with(reportEntry) {
            iprintln("[${severity.displayName}]".padEnd(padding) + message)
            positions.forEach {
                iprintln(it.toString().indent(padding, indentStr = " "))
            }

            thrown?.printStackTrace(stream)
        }

        if (reportEntry.severity == Severity.FAIL) {
            stream.flush()
            throw DoExitNowError()
        }
    }

    private fun printContextHeader(contextStr: ReportingContext) {
        stream.print("[${contextStr.key}]")
        if (contextStr.suffix != null) stream.println(" ${contextStr.suffix}")
        else stream.println()
        contextPrinted = true
    }
}
