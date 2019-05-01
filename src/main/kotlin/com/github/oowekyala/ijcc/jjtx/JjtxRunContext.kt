package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile

/**
 * @author Clément Fournier
 */
class JjtxRunContext(val jjtxParams: JjtxParams,
                     val grammarFile: JccFile) {


    val errorCollector = ErrorCollector(this)

    val jjtxOptsModel: JjtxOptsModel =
        jjtxParams.jjtxConfigFile?.let {
            // TODO don't swallow errors
            JjtxOptsModel.parse(this, it)
        } ?: JjtxOptsModel.default(this)

    override fun toString(): String = "Run context[$jjtxParams]"

}
