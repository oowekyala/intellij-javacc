package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.util.LineAndColumn
import com.google.googlejavaformat.java.Formatter

// todo make service
interface SourceFormatter {

    /**
     * Returns the formatted source.
     */
    fun format(source: String): String

    /**
     * Attempts to retrieve at least line and column from an error message.
     */
    fun parseMessage(errorMessage: String): Pair<LineAndColumn, String>?




    /**
     * Name, case doesn't matter.
     */
    val name: String

}


/**
 * Lists the formatters available to postprocess generated files.
 * These can be referred to in a case-insensitive fashion.
 */
enum class FormatterRegistry : SourceFormatter {
    JAVA {

        private val lcRegex = Regex("(\\d+):(\\d+): error:(.*)")

        override fun format(source: String): String = Formatter().formatSource(source)

        override fun parseMessage(errorMessage: String): Pair<LineAndColumn, String>? =
            lcRegex.find(errorMessage.trim())?.groupValues?.let {
                Pair((it[1].toInt() - 1) to it[2].toInt(), it[3])
            }
    };

    companion object {
        /**
         * Returns the formatter corresponding to the [key],
         * or null if there is none.
         */
        fun select(key: String?): SourceFormatter? {
            val k = key?.toUpperCase() ?: return null
            return values().firstOrNull { it.name == k }
        }
    }
}
