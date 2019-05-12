package com.github.oowekyala.jjtx.util.io

import com.intellij.openapi.util.text.StringUtil
import java.io.FilterOutputStream
import java.io.OutputStream

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
