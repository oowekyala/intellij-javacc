package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.util.io.exists
import java.nio.file.Path

/**
 * @author Clément Fournier
 */
class JjtxRunContext(val jjtxParams: JjtxParams,
                     val grammarFile: JccFile) {


    val errorCollector = ErrorCollector(this)

    val jjtxOptsModel: JjtxOptsModel =
        jjtxParams
            .configChain
            .filter { it.exists() }
            .fold<Path, JjtxOptsModel>(OldJavaccOptionsModel(grammarFile.grammarOptions)) { model, path ->
                JjtxOptsModel.parse(this, path.toFile(), model) ?: model
            }

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
