package com.github.oowekyala.jjtx.util

import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import kotlin.system.exitProcess


data class Io(
    val wd: Path = workingDirectory,
    val stdout: PrintStream = System.out,
    val stderr: PrintStream = System.err,
    val exit: (String, Int) -> Nothing = { _, code -> exitProcess(code) },
    val rootLogger: Logger = Logger.getLogger("jjtricks")
) {

    fun exit(code: Int): Nothing = exit("", code)
}


val workingDirectory: Path
    get() = Paths.get(System.getProperty("user.dir"))

