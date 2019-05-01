package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.util.io.exists
import java.nio.file.Path
import java.nio.file.Paths


/**
 * @author Clément Fournier
 */
abstract class JjtxContext(val grammarFile: JccFile, configChain: List<Path>?) {

    abstract val errorCollector: ErrorCollector

    val grammarName: String = grammarFile.name.replaceAfterLast('.', "")

    val grammarDir: Path = Paths.get(grammarFile.containingDirectory.virtualFile.path)

    val jjtxOptsModel: JjtxOptsModel =
        (configChain ?: JjtxParams.defaultConfigChain(grammarDir, grammarName))
            .filter { it.exists() }
            .fold<Path, JjtxOptsModel>(OldJavaccOptionsModel(grammarFile.grammarOptions.inlineBindings)) { model, path ->
                JjtxOptsModel.parse(this, path.toFile(), model) ?: model
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
