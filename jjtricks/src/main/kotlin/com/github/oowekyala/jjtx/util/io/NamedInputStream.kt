package com.github.oowekyala.jjtx.util.io

import com.github.oowekyala.jjtx.util.inputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Path

/**
 * @param identity Key for the [equals] comparison
 *
 * @author Clément Fournier
 */
class NamedInputStream(
    private val inputStream: () -> InputStream,
    val filename: String,
    private val identity: Any
) {

    fun newInputStream(): InputStream = inputStream()

    override fun toString(): String = "NamedInputStream($filename)"

    /*
        Returns true if the identity keys are equal.
     */
    override fun equals(other: Any?): Boolean = (other as? NamedInputStream)?.identity == this.identity

}

val NamedInputStream.extension
    get() = filename.substringAfterLast('.')

fun NamedInputStream.readText(charset: Charset = Charsets.UTF_8) =
    newInputStream().bufferedReader(charset).use { it.readText() }

fun Path.namedInputStream() =
    NamedInputStream(
        inputStream = this::inputStream,
        filename = toString(),
        identity = this.toAbsolutePath()
    )

fun String.namedInputStream(filename: String = "input"): NamedInputStream =
    NamedInputStream(
        inputStream = { byteInputStream() },
        filename = filename,
        identity = this
    )


