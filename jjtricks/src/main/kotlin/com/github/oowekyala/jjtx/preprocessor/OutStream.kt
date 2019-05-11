package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.ijcc.util.init
import java.io.OutputStream
import java.io.PrintStream

internal class OutStream(
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
