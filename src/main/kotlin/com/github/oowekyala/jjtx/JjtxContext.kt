package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import java.nio.file.Path
import java.nio.file.Paths


/**
 * @author Clément Fournier
 */
interface JjtxContext {
    val grammarFile: JccFile

    val errorCollector: ErrorCollector

    val grammarName: String
        get() = grammarFile.name.replaceAfterLast('.', "")

    val grammarDir: Path
        get() = Paths.get(grammarFile.containingDirectory.virtualFile.path)

    val jjtxOptsModel: JjtxOptsModel

    fun runTemplates(outputDir: Path) {
        for (visitor in jjtxOptsModel.visitors) {
            try {
                visitor.execute(this, outputDir)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
