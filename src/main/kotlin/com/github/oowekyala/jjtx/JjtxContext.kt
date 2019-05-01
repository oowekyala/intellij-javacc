package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.util.io.exists
import java.nio.file.Path
import java.nio.file.Paths


/**
 * @author Clément Fournier
 */
abstract class JjtxContext(val grammarFile: JccFile, configChain: List<Path>?) {

    abstract val errorCollector: ErrorCollector

    val grammarName: String = grammarFile.name.splitAroundFirst('.').first

    val grammarDir: Path = Paths.get(grammarFile.virtualFile.path).parent

    val jjtxOptsModel: JjtxOptsModel by lazy {
        (configChain ?: JjtxParams.defaultConfigChain(grammarDir, grammarName))
            .filter { it.exists() }
            .fold<Path, JjtxOptsModel>(OldJavaccOptionsModel(InlineGrammarOptions(grammarFile))) { model, path ->
                JjtxOptsModel.parse(this, path.toFile(), model) ?: model
            }
    }


    fun runTemplates(outputDir: Path) {
        for (visitor in jjtxOptsModel.visitors) {
            try {
                visitor.execute(this, outputDir)
            } catch (e: Exception) {
                // FIXME report cleanly
                e.printStackTrace()
            }
        }
    }

}
