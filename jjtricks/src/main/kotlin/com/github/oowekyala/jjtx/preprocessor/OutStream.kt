package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.ijcc.util.init
import com.intellij.openapi.util.text.StringUtil
import java.io.FilterOutputStream
import java.io.OutputStream
import java.io.PrintStream

/**
 * DSL for a printstream, easier to read and write.
 */
internal class OutStream(
    outputStream: OutputStream,
    private val baseIndentString: String
) : PrintStream(TrailingSpacesFilterOutputStream(outputStream)) {

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


    operator fun String.unaryPlus(): OutStream {
        print(indentString)
        print(this)
        return this@OutStream
    }

    operator fun String.unaryMinus(): OutStream {
        print(this)
        return this@OutStream
    }

    operator fun plus(other: String): OutStream {
        print(other)
        return this@OutStream
    }

    operator fun plus(endl: Endl): OutStream {
        println()
        return this@OutStream
    }


    inline operator fun plus(e: OutStream.() -> Unit): OutStream {
        println("{")
        indentString += baseIndentString
        e()
        indentString = indentString.removeSuffix(baseIndentString)
        print(indentString)
        print("}")
        return this
    }

    object Endl

}

/**
 * Filters trailing spaces.
 */
internal class TrailingSpacesFilterOutputStream(out: OutputStream) : FilterOutputStream(out) {

    private val curLine = StringBuilder()

    override fun write(b: Int) {
        if (b == '\n'.toInt() || b == '\r'.toInt())
            flushLine(b)
        else curLine.appendCodePoint(b)
    }

    private fun flushLine(endl: Int) {
        super.out.write(StringUtil.trimTrailing(curLine, ' ').toString().toByteArray())
        curLine.clear()
        super.out.write(endl)
    }

    override fun flush() {
        if (curLine.isNotEmpty()) {
            super.out.write(curLine.toString().toByteArray())
            curLine.clear()
        }
    }
}
