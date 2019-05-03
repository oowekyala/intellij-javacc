package com.github.oowekyala.jjtx.util

import kotlinx.cli.HelpPrinter
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess


data class Io(
    val wd: Path = workingDirectory,
    val stdout: PrintStream = System.out,
    val stderr: PrintStream = System.err,
    val exit: (Int) -> Nothing = ::exitProcess
) {

    fun helpPrinter(syntaxWidth: Int = 24): HelpPrinter = CustomHelpPrinter(syntaxWidth)

    private inner class CustomHelpPrinter(private val syntaxWidth: Int) : HelpPrinter {

        override fun printText(text: String) {
            stdout.println(text)
        }

        override fun printSeparator() {
            stdout.println()
        }

        override fun printEntry(helpEntry: String, description: String) {
            if (helpEntry.length <= syntaxWidth) {
                stdout.println("  ${helpEntry.padEnd(syntaxWidth)}  $description")
            } else {
                stdout.println("  $helpEntry")
                stdout.println("  ${"".padEnd(syntaxWidth)}  $description")
            }
        }
    }
}


val workingDirectory: Path
    get() = Paths.get(System.getProperty("user.dir"))
