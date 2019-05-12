package com.github.oowekyala.jjtx.util.io

/**
 * Exit code of the app.
 */
enum class ExitCode {
    OK,
    ERROR;

    val toInt = ordinal
}
