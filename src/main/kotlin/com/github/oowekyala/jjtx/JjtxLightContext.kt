package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.util.io.exists


/**
 * @author Clément Fournier
 */
class JjtxLightContext(override val grammarFile: JccFile) : JjtxContext {


    override val errorCollector = object : ErrorCollector {
        override fun handleError(message: String,
                                 category: ErrorCollector.Category,
                                 severityOverride: ErrorCollector.Severity?,
                                 vararg sourcePosition: Position): ErrorCollector.Severity {
            // do nothing
            return severityOverride ?: category.minSeverity
        }

    }


    override val jjtxOptsModel: JjtxOptsModel

    init {

        val top = OldJavaccOptionsModel(grammarFile.grammarOptions)


        jjtxOptsModel =
            grammarDir.resolve("$grammarName.jjtopts.yaml")
                .takeIf { it.exists() }
                ?.let {
                    JjtxOptsModel.parse(this, it.toFile(), top)
                } ?: top

    }


}
