package com.github.oowekyala.jjtx.templates

import com.google.googlejavaformat.java.Formatter

/**
 * Lists the formatters available to postprocess generated files.
 * These can be referred to in a case-insensitive fashion.
 */
enum class FormatterChoice(private val doFormat: (String) -> String) {
    JAVA({ Formatter().formatSource(it) });

    /**
     * Returns the formatted source.
     */
    fun format(source: String) = doFormat(source)

    companion object {
        /**
         * Returns the formatter corresponding to the [key],
         * or null if there is none.
         */
        fun select(key: String?): FormatterChoice? {
            val k = key?.toUpperCase() ?: return null
            return values().firstOrNull { it.name == k }
        }
    }
}
