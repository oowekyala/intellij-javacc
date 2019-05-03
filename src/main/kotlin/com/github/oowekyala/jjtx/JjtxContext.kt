package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.util.ErrorCollector
import com.github.oowekyala.jjtx.util.Io
import com.intellij.util.io.isFile
import java.nio.file.Path
import java.nio.file.Paths


/**
 * @param configChain The config chain, in increasing precedence order
 *
 * @author Clément Fournier
 */
abstract class JjtxContext(val grammarFile: JccFile, configChain: List<Path>) {

    abstract val errorCollector: ErrorCollector

    abstract val io: Io

    val grammarName: String = grammarFile.virtualFile.nameWithoutExtension

    val grammarDir: Path = Paths.get(grammarFile.virtualFile.path).parent

    val jjtxOptsModel: JjtxOptsModel by lazy {
        configChain
            .asReversed()
            .filter { it.isFile() }
            .fold<Path, JjtxOptsModel>(OldJavaccOptionsModel(grammarFile)) { model, path ->
                JjtxOptsModel.parse(this, path, model) ?: model
            }
    }

}
