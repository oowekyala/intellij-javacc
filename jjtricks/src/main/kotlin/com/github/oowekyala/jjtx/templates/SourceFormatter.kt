package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.reporting.ErrorPositionRecoverer
import com.github.oowekyala.jjtx.reporting.GJFormatExtractor
import com.google.googlejavaformat.java.Formatter

// todo make service
interface SourceFormatter {

    /**
     * Returns the formatted source.
     */
    fun format(source: String): String

    val errorPositionParser: ErrorPositionRecoverer?


    /**
     * Name, case doesn't matter.
     */
    val name: String

}


/**
 * Lists the formatters available to postprocess generated files.
 * These can be referred to in a case-insensitive fashion.
 */
enum class FormatterRegistry(override val errorPositionParser: ErrorPositionRecoverer?) : SourceFormatter {
    JAVA(GJFormatExtractor) {
        override fun format(source: String): String = Formatter().formatSource(source)
    };

    companion object {
        /**
         * Returns the formatter corresponding to the [key],
         * or null if there is none.
         */
        fun getOrDefault(key: String?): SourceFormatter? {
            val k = key?.toUpperCase() ?: return JAVA
            return values().firstOrNull { it.name == k } ?: JAVA
        }
    }
}
