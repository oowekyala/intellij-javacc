package com.github.oowekyala.jjtx.util

import kotlinx.cli.HelpPrinter
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import kotlin.system.exitProcess


data class Io(
    val wd: Path = workingDirectory,
    val stdout: PrintStream = System.out,
    val stderr: PrintStream = System.err,
    val exit: (Int) -> Nothing = ::exitProcess,
    val rootLogger: Logger = Logger.getLogger("jjtricks")
) {

    fun helpPrinter(syntaxWidth: Int = 24, totalWidth: Int = 80): HelpPrinter =
        CustomHelpPrinter(syntaxWidth, totalWidth)

    private inner class CustomHelpPrinter(private val syntaxWidth: Int, private val totalWidth: Int) : HelpPrinter {

        private val restLen = totalWidth - syntaxWidth
        private val syntaxIndent = "    "

        override fun printText(text: String) {
            stdout.println(text.wrap(totalWidth))
        }

        override fun printSeparator() {
            stdout.println()
        }

        override fun printEntry(helpEntry: String, description: String) {
            if (helpEntry.length <= syntaxWidth - syntaxIndent.length) {
                stdout.println(syntaxIndent + helpEntry.padEnd(syntaxWidth) + syntaxIndent + wrapDescription(description))
            } else {
                stdout.println(syntaxIndent + helpEntry)
                stdout.println(syntaxIndent + " ".repeat(syntaxWidth) + syntaxIndent + wrapDescription(description))
            }
        }

        private fun wrapDescription(description: String) = description.wrap(restLen, syntaxWidth + syntaxIndent.length * 2 + 1)
    }
}


val workingDirectory: Path
    get() = Paths.get(System.getProperty("user.dir"))

