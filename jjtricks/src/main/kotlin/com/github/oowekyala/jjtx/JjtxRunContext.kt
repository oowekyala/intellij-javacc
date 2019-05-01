package com.github.oowekyala.jjtx

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


    fun runTemplates() {
        for (visitor in jjtxOptsModel.visitors) {
            try {
                visitor.execute(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
