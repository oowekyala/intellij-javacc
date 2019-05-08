package com.github.oowekyala.jjtx.util

import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess


/**
 * Encapsulates the IO context of a running app.
 * Can be used to start several instances of the app
 * in the same VM, or eg to redirect the output streams.
 *
 * The default constructor parameters represent the standard
 * JVM IO context.
 */
data class Io(
    val wd: Path = workingDirectory,
    val stdout: PrintStream = System.out,
    val stderr: PrintStream = System.err,
    private val exit: (String, Int) -> Nothing = { m, code -> stderr.println(m); stderr.flush(); exitProcess(code) }
) {

    fun exit(code: ExitCode): Nothing = exit("", code.toInt)

    fun exit(message: String, code: ExitCode): Nothing = exit(message, code.toInt)

    fun bail(message: String): Nothing {
        exit(message, ExitCode.ERROR)
    }

    fun bail(throwable: Throwable, showStackTrace: Boolean): Nothing {
        if (showStackTrace) {
            throwable.printStackTrace(stderr)
            exit("", ExitCode.ERROR)
        } else {
            exit(throwable.message ?: "", ExitCode.ERROR)
        }
    }
}


val workingDirectory: Path
    get() = Paths.get(System.getProperty("user.dir"))

/**
 * Exit code of the app.
 */
enum class ExitCode {
    OK,
    ERROR;

    val toInt = ordinal
}
