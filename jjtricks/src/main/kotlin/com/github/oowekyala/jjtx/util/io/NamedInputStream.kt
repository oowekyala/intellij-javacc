package com.github.oowekyala.jjtx.util.io

import java.io.InputStream
import java.nio.charset.Charset

/**
 * @author Clément Fournier
 */
class NamedInputStream(
    private val inputStream: () -> InputStream,
    val filename: String
) {

    fun newInputStream(): InputStream = inputStream()

    override fun toString(): String = "NamedInputStream($filename)"

    val extension
        get() = filename.substringAfterLast('.')
}

fun NamedInputStream.readText(charset: Charset = Charsets.UTF_8) =
    newInputStream().bufferedReader(charset).use { it.readText() }


fun namedInputStream(input: String, filename: String = "input"): NamedInputStream =
    NamedInputStream(
        { input.byteInputStream() },
        filename
    )


