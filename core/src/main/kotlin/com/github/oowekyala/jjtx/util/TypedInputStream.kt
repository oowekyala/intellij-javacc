package com.github.oowekyala.jjtx.util

import java.io.InputStream

/**
 * @author Clément Fournier
 */
data class NamedInputStream(
    val inputStream: InputStream,
    val filename: String
)


val NamedInputStream.extension
get() = filename.substringAfterLast('.')
