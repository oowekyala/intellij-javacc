package com.github.oowekyala.jjtx.util.io

import java.io.InputStream

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


