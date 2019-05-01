package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile


/**
 * @author Clément Fournier
 */
class JjtxLightContext(grammarFile: JccFile) : JjtxContext(grammarFile, null) {


    override val errorCollector = object : ErrorCollector {
        override fun handleError(message: String,
                                 category: ErrorCollector.Category,
                                 severityOverride: ErrorCollector.Severity?,
                                 vararg sourcePosition: Position): ErrorCollector.Severity {
            // do nothing
            return severityOverride ?: category.minSeverity
        }

    }

}
