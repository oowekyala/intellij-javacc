package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile

/**
 * @author Clément Fournier
 */
class JjtxRunContext(val jjtxParams: JjtxParams,
                     val grammarFile: JccFile) {


    val errorCollector = ErrorCollector()

    val jjtxOptsModel: JjtxOptsModel =
        jjtxParams.jjtxConfigFile?.let {
            JjtxOptsModel.parse(it)
        } ?: JjtxOptsModel.default()


}
