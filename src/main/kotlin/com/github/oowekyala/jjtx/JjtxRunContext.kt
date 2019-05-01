package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.util.io.exists
import java.nio.file.Path

/**
 * @author Clément Fournier
 */
class JjtxRunContext(jjtxParams: JjtxParams,
                     override val grammarFile: JccFile) : JjtxContext {


    override val errorCollector = ErrorCollectorImpl(this)

    override val jjtxOptsModel: JjtxOptsModel =
        jjtxParams
            .configChain
            .filter { it.exists() }
            .fold<Path, JjtxOptsModel>(OldJavaccOptionsModel(grammarFile.grammarOptions)) { model, path ->
                JjtxOptsModel.parse(this, path.toFile(), model) ?: model
            }

}
