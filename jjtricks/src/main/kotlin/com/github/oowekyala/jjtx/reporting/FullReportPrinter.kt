package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.jjtx.util.baseIndent
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.PrintStream

/**
 * Prints every message without aggregation, also used as an exception
 * printer by [AggregateReportPrinter].
 */
class FullReportPrinter(
    private val stream: PrintStream,
    private val minSeverity: Severity,
    private val contextStr: ReportingContext? = null,
    private val printStackTrace: Boolean = true,
    private val indent: String = ""
) : MessageCollector {

    private var contextPrinted = contextStr == null


    private fun iprintln(string: String) {
        stream.print(indent)
        stream.println(string)
    }

    private val lcolOffset: Int = indent.length + lcolWidth
    private val lcolIndent = " ".repeat(lcolOffset)

    /**
     * Doesn't maintain state so can return a new instance.
     */
    override fun withContext(contextStr: ReportingContext): MessageCollector =
        FullReportPrinter(
            stream = stream,
            minSeverity = minSeverity,
            contextStr = contextStr,
            printStackTrace = printStackTrace,
            indent = baseIndent
        )


    override fun concludeReport() {
        stream.flush()
    }

    fun printExceptionPosition(position: Position) {
        stream.println(position.toString().replaceIndent(lcolIndent))
    }

    override fun reportEntry(reportEntry: ReportEntry) {
        if (reportEntry.severity < minSeverity) return

        if (!contextPrinted && contextStr != null) {
            printContextHeader(contextStr)
        }

        with(reportEntry) {
            val header = if (thrown != null) ExceptionNameInterpreter.getHeader(thrown) else message ?: return

            iprintln("[${severity.displayName}]".padEnd(lcolWidth) + header)
            if (thrown != null && message != null) {
                stream.println(message.trim().replaceIndent(lcolIndent))
            }

            positions.forEach {
                printExceptionPosition(it)
            }

            if (printStackTrace) {
                thrown?.let {
                    val st = ExceptionUtils.getStackTrace(it)
                    it.message?.let { st.removePrefix(it) } ?: st
                }
                    ?.let {
                        stream.println(it.replaceIndent(lcolIndent + baseIndent))
                    }
            }
        }

        if (reportEntry.severity == Severity.FAIL) {
            throw DoExitNowError()
        }
    }

    private fun printContextHeader(contextStr: ReportingContext) {
        stream.print("[${contextStr.key}]")
        if (contextStr.suffix != null) stream.println(" ${contextStr.suffix}")
        else stream.println()
        contextPrinted = true
    }

    private companion object {
        val lcolWidth = Severity.values().map { it.displayName.length }.max()!! + 4
    }
}
