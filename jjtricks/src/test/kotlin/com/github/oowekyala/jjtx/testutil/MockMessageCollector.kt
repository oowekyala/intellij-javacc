package com.github.oowekyala.jjtx.testutil

import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.reporting.ReportEntry
import com.github.oowekyala.jjtx.reporting.ReportingContext

/**
 * @author Clément Fournier
 */
class MockMessageCollector : MessageCollector {

    val entries = mutableListOf<ReportEntry>()

    override fun withContext(contextStr: ReportingContext): MessageCollector = this

    override fun reportEntry(reportEntry: ReportEntry) {
        entries += reportEntry
    }

    override fun concludeReport() {
    }

}
