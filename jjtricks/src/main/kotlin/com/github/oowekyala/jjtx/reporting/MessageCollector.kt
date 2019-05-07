package com.github.oowekyala.jjtx.reporting

import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.Position
import java.util.*


interface MessageCollector {

    /**
     * @param message arg for the message
     * @param category determines min severity & message
     * @param severityOverride Force this severity to be used
     * @param sourcePosition to report
     *
     * @return The actual severity reported
     */
    fun report(message: String,
               category: ErrorCategory,
               severityOverride: Severity? = null,
               vararg sourcePosition: Position?): Severity

    fun report(message: String,
               category: ErrorCategory,
               vararg sourcePosition: Position?): Severity =
        report(message, category, severityOverride = null, sourcePosition = *sourcePosition)

    /**
     * Report a normal execution trace.
     */
    fun reportNormal(message: String) {
        report(message, ErrorCategory.NORMAL_EXEC_MESSAGE, severityOverride = null)
    }


    fun concludeReport()


    companion object {

        fun noop(): MessageCollector {
            return object : MessageCollector {

                override fun report(message: String,
                                    category: ErrorCategory,
                                    severityOverride: Severity?,
                                    vararg sourcePosition: Position?): Severity =
                    // do nothing
                    severityOverride ?: category.minSeverity

                override fun concludeReport() {
                    // do nothing
                }
            }
        }

        fun create(io: Io, aggregate: Boolean, minSeverity: Severity): MessageCollector {

            val output = io.stderr
            val printer = if (aggregate) AggregateReportPrinter(output) else FullReportPrinter(output)

            return MessageCollectorImpl(
                printer,
                minSeverity
            )
        }
    }
}


/**
 * @property minSeverity Minimum severity on which to report
 *
 * @author Clément Fournier
 */
private class MessageCollectorImpl(
    private val reportPrinter: ReportPrinter,
    private val minSeverity: Severity = Severity.WARNING
) : MessageCollector {

    override fun concludeReport() {
        reportPrinter.onEnd()
    }

    override fun report(message: String,
                        category: ErrorCategory,
                        severityOverride: Severity?,
                        vararg sourcePosition: Position?): Severity {

        val realSeverity = severityOverride ?: category.minSeverity

        if (realSeverity == Severity.FAIL) {
            throw IllegalStateException(message) // TODO add position
        }

        if (realSeverity < minSeverity) return Severity.IGNORE

        reportPrinter.printEntry(
            ReportEntry(
                category,
                message,
                realSeverity,
                listOfNotNull(*sourcePosition),
                Date()
            )
        )

        return realSeverity
    }
}

enum class Severity {
    IGNORE,
    FINE,
    WARNING,
    NORMAL,
    FAIL
}

enum class ErrorCategory(val minSeverity: Severity) {
    /** Regex pattern in jjtopts doesn't match any jjtree node in grammar. */
    UNMATCHED_HIERARCHY_REGEX(Severity.WARNING),
    /** Exact node name in jjtopts doesn't match any jjtree node in grammar. */
    EXACT_NODE_NOT_IN_GRAMMAR(Severity.FINE),
    /**
     * Regex pattern in jjtopts should be a leaf.
     * Just a warning if it matches exactly one name.
     */
    REGEX_SHOULD_BE_LEAF(Severity.WARNING),

    UNCOVERED_NODE(Severity.WARNING),

    MULTIPLE_HIERARCHY_ROOTS(Severity.FAIL),
    PARSING_ERROR(Severity.FAIL),
    FORMATTER_ERROR(Severity.FAIL),

    DUPLICATE_MATCH(Severity.WARNING),
    INVALID_REGEX(Severity.WARNING),
    FILE_NOT_FOUND(Severity.WARNING),


    NO_HIERARCHY_ROOTS(Severity.FINE),
    WRONG_TYPE(Severity.FINE),
    VISITOR_NOT_RUN(Severity.FINE),
    VISITOR_GENERATED(Severity.FINE),
    INCOMPLETE_VISITOR_SPEC(Severity.FINE),


    NORMAL_EXEC_MESSAGE(Severity.NORMAL)
}
