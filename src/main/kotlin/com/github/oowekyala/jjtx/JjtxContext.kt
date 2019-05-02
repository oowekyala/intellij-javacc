package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.ErrorCollector.Category.VISITOR_NOT_RUN
import com.github.oowekyala.jjtx.templates.GrammarBean
import com.github.oowekyala.jjtx.templates.set
import com.intellij.util.io.exists
import org.apache.velocity.VelocityContext
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
            .asReversed()
            .filter { it.exists() }
            .fold<Path, JjtxOptsModel>(OldJavaccOptionsModel(InlineGrammarOptions(grammarFile))) { model, path ->
                JjtxOptsModel.parse(this, path.toFile(), model) ?: model
            }
    }


    fun runTemplates(outputDir: Path) {

        val globalCtx = VelocityContext()
        globalCtx["grammar"] = GrammarBean.create(this)
        globalCtx["global"] = jjtxOptsModel.templateContext
        globalCtx["H"] = "#"


        for ((id, visitor) in jjtxOptsModel.visitors) {

            if (!visitor.execute) {
                errorCollector.handleError("Visitor $id is not configured for execution", VISITOR_NOT_RUN)
                continue
            }

            try {
                visitor.execute(this, globalCtx, outputDir)
            } catch (e: Exception) {
                // FIXME report cleanly
                e.printStackTrace()
            }
        }
    }

}
