package com.github.oowekyala.jjtx.reporting

enum class MessageCategory(val minSeverity: Severity) {
    /** Regex pattern in jjtopts doesn't match any jjtree node in grammar. */
    UNMATCHED_HIERARCHY_REGEX(Severity.WARNING),
    /** Exact node name in jjtopts doesn't match any jjtree node in grammar. */
    EXACT_NODE_NOT_IN_GRAMMAR(Severity.FINE),
    /**
     * Regex pattern in jjtopts should be a leaf.
     * Just a warning if it matches exactly one name.
     */
    REGEX_SHOULD_BE_LEAF(Severity.WARNING),

    UNCOVERED_NODE(Severity.FINE),
    UNCOVERED_GEN_NODE(Severity.WARNING),
    DUPLICATE_MATCH(Severity.FINE),
    NO_MATCH(Severity.WARNING),

    MULTIPLE_HIERARCHY_ROOTS(Severity.FAIL),
    PARSING_ERROR(Severity.FAIL),
    FORMATTER_ERROR(Severity.NON_FATAL),

    INVALID_REGEX(Severity.NON_FATAL),
    FILE_NOT_FOUND(Severity.WARNING),


    NO_HIERARCHY_ROOTS(Severity.FINE),
    WRONG_TYPE(Severity.NON_FATAL),
    VISITOR_NOT_RUN(Severity.FINE),
    VISITOR_GENERATED(Severity.FINE),
    INCOMPLETE_VISITOR_SPEC(Severity.FINE),

    CLASS_GENERATED(Severity.FINE),
    CLASS_NOT_GENERATED(Severity.FINE),

    NORMAL_EXEC_MESSAGE(Severity.NORMAL),
    NON_FATAL(Severity.NON_FATAL)
}
