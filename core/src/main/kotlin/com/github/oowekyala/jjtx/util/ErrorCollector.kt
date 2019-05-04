package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.JjtxRunContext


interface ErrorCollector {
    fun handleError(message: String,
                    category: ErrorCategory,
                    severityOverride: Severity? = null,
                    vararg sourcePosition: Position?): Severity

}


/**
 * @property minSeverity Minimum severity on which to report
 *
 * @author Clément Fournier
 */
class ErrorCollectorImpl(val ctx: JjtxRunContext, private val minSeverity: Severity)
    : ErrorCollector {


    /**
     * @param message arg for the message
     * @param category determines min severity & message
     * @param severityOverride Force this severity to be used
     * @param sourcePosition to report
     *
     * @return The actual severity reported
     */
    override fun handleError(message: String,
                             category: ErrorCategory,
                             severityOverride: Severity?,
                             vararg sourcePosition: Position?): Severity {

        val realSeverity = severityOverride ?: category.minSeverity

        if (realSeverity < minSeverity) return Severity.IGNORE

        ctx.io.stderr.println("$category: $message")
        sourcePosition.filterNotNull().forEach {
            ctx.io.stderr.println("\t${it.toString(ctx)}")
        }

        return realSeverity
    }


}

enum class Severity {
    IGNORE,
    INFO,
    WARN,
    FAIL
}

enum class ErrorCategory(val minSeverity: Severity) {
    /** Regex pattern in jjtopts doesn't match any jjtree node in grammar. */
    UNMATCHED_HIERARCHY_REGEX(Severity.WARN),
    /** Exact node name in jjtopts doesn't match any jjtree node in grammar. */
    EXACT_NODE_NOT_IN_GRAMMAR(Severity.INFO),
    /**
     * Regex pattern in jjtopts should be a leaf.
     * Just a warning if it matches exactly one name.
     */
    REGEX_SHOULD_BE_LEAF(Severity.WARN),

    UNCOVERED_NODE(Severity.WARN),

    MULTIPLE_HIERARCHY_ROOTS(Severity.FAIL),

    NO_HIERARCHY_ROOTS(Severity.INFO),
    WRONG_TYPE(Severity.INFO),

    DUPLICATE_MATCH(Severity.WARN),
    INVALID_REGEX(Severity.WARN),
    FILE_NOT_FOUND(Severity.WARN),

    VISITOR_NOT_RUN(Severity.INFO),
    INCOMPLETE_VISITOR_SPEC(Severity.INFO),
}
