package com.github.oowekyala.jjtx.util

sealed class StringSource {

    data class File(val fname: String) : StringSource()
    data class Str(val source: String) : StringSource()

    companion object {

        fun ofTrimmed(s: () -> String) = Str(s().trim())

    }

}
