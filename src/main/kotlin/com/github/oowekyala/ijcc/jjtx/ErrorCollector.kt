package com.github.oowekyala.ijcc.jjtx

/**
 * @author Clément Fournier
 */
class ErrorCollector {


    /**
     * @param message arg for the message
     * @param category determines min severity & message
     * @param severityOverride Force this severity to be used
     * @param sourcePosition to report
     *
     * @return The actual severity reported
     */
    fun handleError(message: String,
                    category: Category,
                    severityOverride: Severity? = null,
                    sourcePosition: SourcePosition? = null): Severity {
        println("$category: $message")
        return severityOverride ?: category.minSeverity
    }

    enum class Category(val minSeverity: Severity) {
        /** Regex pattern in jjtopts doesn't match any jjtree node in grammar. */
        UNMATCHED_HIERARCHY_REGEX(Severity.WARN),
        /** Exact node name in jjtopts doesn't match any jjtree node in grammar. */
        UNMATCHED_EXACT_NODE(Severity.INFO),
        /**
         * Regex pattern in jjtopts should be a leaf.
         * Just a warning if it matches exactly one name.
         */
        REGEX_SHOULD_BE_LEAF(Severity.WARN),

        MULTIPLE_HIERARCHY_ROOTS(Severity.FAIL),
        NO_HIERARCHY_ROOTS(Severity.INFO),
    }

    enum class Severity {
        IGNORE,
        INFO,
        WARN,
        FAIL
    }

}
