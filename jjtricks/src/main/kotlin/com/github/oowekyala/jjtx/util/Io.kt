package com.github.oowekyala.jjtx.util

import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import kotlin.system.exitProcess


/**
 * Encapsulates the IO context of a running app.
 * Can be used to start several instances of the app
 * in the same VM, or eg to redirect the output streams.
 */
data class Io(
    val wd: Path = workingDirectory,
    val stdout: PrintStream = System.out,
    val stderr: PrintStream = System.err,
    private val exit: (String, Int) -> Nothing = { _, code -> exitProcess(code) }
) {

    fun exit(code: ExitCode): Nothing = exit("", code.toInt)

    fun exit(message: String, code: ExitCode): Nothing = exit(message, code.toInt)

    fun bail(message: String): Nothing {
        stderr.println(message)
        stderr.flush()
        exit(message, ExitCode.ERROR)
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
