package com.github.oowekyala.jjtx.util

import com.github.oowekyala.ijcc.util.indent
import com.github.oowekyala.jjtx.JjtxRunContext
import java.io.PrintStream


open class MessageCollector {

    /**
     * @param message arg for the message
     * @param category determines min severity & message
     * @param severityOverride Force this severity to be used
     * @param sourcePosition to report
     *
     * @return The actual severity reported
     */
    open fun report(message: String,
                    category: ErrorCategory,
                    severityOverride: Severity? = null,
                    vararg sourcePosition: Position?): Severity =
        severityOverride ?: category.minSeverity

    fun report(message: String,
               category: ErrorCategory,
               vararg sourcePosition: Position?): Severity =
        report(message, category, severityOverride = null, sourcePosition = *sourcePosition)


    open fun printReport(stream: PrintStream) {
        // do nothing
    }

}


/**
 *
 * @property aggregateEntries Whether to just print the number of warnings issued
 * @property minSeverity Minimum severity on which to report
 *
 * @author Clément Fournier
 */
class MessageCollectorImpl(val ctx: JjtxRunContext,
                           private val aggregateEntries: Boolean = true,
                           private val minSeverity: Severity = Severity.WARNING) : MessageCollector() {

    private val aggregate = mutableMapOf<Severity, MutableList<ReportEntry>>()

    private val padding = Severity.values().map { it.name.length }.max()!! + 4

    override fun printReport(stream: PrintStream) {
        if (aggregateEntries) {

            val warnings = aggregate[Severity.WARNING] ?: mutableListOf()

            if (warnings.isEmpty()) {
                return
            } else {
                val sizes = warnings.size
                stream.println("There are $sizes JJTricks warnings")
                stream.println("Rerun with --warn option to examine them")
            }
        } else {
            for (entry in aggregate.values.flatten()) {
                val (_, message, realSeverity, positions) = entry
                val logLine = "[$realSeverity]".padEnd(padding) + message
                stream.println(logLine)
                positions.forEach {
                    stream.println(it.toString(ctx).indent(padding, indentStr = " "))
                }
            }
        }
        stream.flush()
    }

    override fun report(message: String,
                        category: ErrorCategory,
                        severityOverride: Severity?,
                        vararg sourcePosition: Position?): Severity {

        val realSeverity = severityOverride ?: category.minSeverity

        if (realSeverity == Severity.FAIL) {
            throw IllegalStateException(message)
        }

        if (realSeverity < minSeverity) return Severity.IGNORE

        aggregate.computeIfAbsent(realSeverity) { mutableListOf() } += ReportEntry(
            category,
            message,
            realSeverity,
            listOfNotNull(*sourcePosition)
        )

        return realSeverity
    }
}

data class ReportEntry(
    val errorCategory: ErrorCategory,
    val message: String,
    val severity: Severity,
    val positions: List<Position>
)

enum class Severity {
    IGNORE,
    INFO,
    WARNING,
    FAIL
}

enum class ErrorCategory(val minSeverity: Severity) {
    /** Regex pattern in jjtopts doesn't match any jjtree node in grammar. */
    UNMATCHED_HIERARCHY_REGEX(Severity.WARNING),
    /** Exact node name in jjtopts doesn't match any jjtree node in grammar. */
    EXACT_NODE_NOT_IN_GRAMMAR(Severity.INFO),
    /**
     * Regex pattern in jjtopts should be a leaf.
     * Just a warning if it matches exactly one name.
     */
    REGEX_SHOULD_BE_LEAF(Severity.WARNING),

    UNCOVERED_NODE(Severity.WARNING),

    MULTIPLE_HIERARCHY_ROOTS(Severity.FAIL),
    PARSING_ERROR(Severity.FAIL),

    DUPLICATE_MATCH(Severity.WARNING),
    INVALID_REGEX(Severity.WARNING),
    FILE_NOT_FOUND(Severity.WARNING),


    NO_HIERARCHY_ROOTS(Severity.INFO),
    WRONG_TYPE(Severity.INFO),
    VISITOR_NOT_RUN(Severity.INFO),
    VISITOR_GENERATED(Severity.INFO),
    INCOMPLETE_VISITOR_SPEC(Severity.INFO),
}
