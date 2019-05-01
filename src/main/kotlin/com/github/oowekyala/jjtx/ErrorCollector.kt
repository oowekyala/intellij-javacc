package com.github.oowekyala.jjtx

/**
 * @author Clément Fournier
 */
class ErrorCollector(val ctx: JjtxRunContext) {


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
                    vararg sourcePosition: Position): Severity {
        System.err.println("$category: $message")
        sourcePosition.forEach {
            System.err.println("\t At ${it.toString(ctx)}")
        }

        return severityOverride ?: category.minSeverity
    }

    enum class Category(val minSeverity: Severity) {
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
    }

    enum class Severity {
        IGNORE,
        INFO,
        WARN,
        FAIL
    }

}
