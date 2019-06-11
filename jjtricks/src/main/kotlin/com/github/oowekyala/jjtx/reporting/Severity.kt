package com.github.oowekyala.jjtx.reporting

enum class Severity(dName: String? = null) {
    /** Special severity */
    IGNORE,
    FINE("DEBUG"),
    WARNING,
    /** Normal execution messages. */
    NORMAL("INFO"),
    NON_FATAL("ERROR"),
    FAIL("ERROR");

    val displayName = dName ?: name
}
