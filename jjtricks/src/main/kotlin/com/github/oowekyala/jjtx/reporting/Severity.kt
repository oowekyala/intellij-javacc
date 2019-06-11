package com.github.oowekyala.jjtx.reporting

enum class Severity(dName: String? = null) {
    /** Special severity */
    IGNORE,
    FINE("DEBUG"),
    WARNING,
    /** Normal execution messages. */
    NORMAL("INFO"),
    NON_FATAL("ERROR"),
    FAIL("FATAL");

    val displayName = dName ?: name
}
