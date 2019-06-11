package com.github.oowekyala.jjtx.util.io

import com.github.oowekyala.ijcc.util.init
import java.io.OutputStream
import java.io.PrintStream

/**
 * DSL for a printstream, easier to read and write.
 */
internal class DslPrintStream private constructor(
    outputStream: OutputStream,
    private val baseIndentString: String
) : PrintStream(outputStream) {

    var indentString: String = ""


    fun printSource(str: String) {
        print(str)
    }

    fun printWhiteOut(str: String) {
        val lines = str.lines()
        if (lines.size <= 1) return
        else {
            for (l in lines.init()) {
                println()
            }
        }
    }


    operator fun String.unaryPlus(): DslPrintStream {
        print(indentString)
        print(this)
        return this@DslPrintStream
    }

    operator fun String.unaryMinus(): DslPrintStream {
        print(this)
        return this@DslPrintStream
    }

    operator fun plus(other: String): DslPrintStream {
        print(other)
        return this@DslPrintStream
    }

    operator fun plus(endl: Endl): DslPrintStream {
        println()
        return this@DslPrintStream
    }


    inline operator fun plus(e: DslPrintStream.() -> Unit): DslPrintStream {
        println("{")
        indentString += baseIndentString
        e()
        indentString = indentString.removeSuffix(baseIndentString)
        print(indentString)
        print("}")
        return this
    }

    /**
     * Represents an end-of-line.
     */
    object Endl

    companion object {

        fun forTestOutput(out: OutputStream): DslPrintStream {
            val baseIndent = "    "
            val os = out.let(::TrailingSpacesFilterOutputStream)

            return DslPrintStream(os, baseIndent)
        }

        fun forJavaccOutput(out: OutputStream): DslPrintStream = DslPrintStream(out.let(::TrailingSpacesFilterOutputStream), "    ")


    }
}
