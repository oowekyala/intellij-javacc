package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.io.Io

interface MessageCollector {

    fun withContext(contextStr: ReportingContext): MessageCollector

    fun reportEntry(reportEntry: ReportEntry)

    fun reportEntry(reportEntry: ExceptionEntry)

    fun concludeReport()


    companion object {

        fun noop(): MessageCollector = NoopCollector

        fun default(io: Io): MessageCollector = create(io, true, Severity.NORMAL)

        fun create(io: Io, aggregate: Boolean, minSeverity: Severity): MessageCollector {

            val output = io.stderr

            return if (aggregate) AggregateReportPrinter(output) else FullReportPrinter(output, minSeverity)
        }
    }
}


private object NoopCollector : MessageCollector {

    override fun withContext(contextStr: ReportingContext) = this

    override fun concludeReport() {
        // do nothing
    }

    override fun reportEntry(reportEntry: ExceptionEntry) {
        // do nothing
    }

    override fun reportEntry(reportEntry: ReportEntry) {
        // do nothing
    }
}
