package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.templates.GrammarBean
import com.github.oowekyala.jjtx.templates.set
import com.github.oowekyala.jjtx.typeHierarchy.TreeLikeWitness
import com.github.oowekyala.jjtx.util.ErrorCategory
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.SimpleTreePrinter
import org.apache.velocity.VelocityContext
import java.nio.file.Path

/**
 * CLI parameters of a JJTX run.
 *
 *
 * @author Clément Fournier
 */
data class JjtxParams(
    val io: Io,
    val mainGrammarFile: JccFile,
    val outputRoot: Path?,
    val configChain: List<Path>
)

sealed class JjtxTask {

    abstract fun execute(ctx: JjtxContext)

}


object DumpConfigTask : JjtxTask() {

    override fun execute(ctx: JjtxContext) {

        val typeHierarchy = ctx.jjtxOptsModel.typeHierarchy

        ctx.io.stdout.println(SimpleTreePrinter(TreeLikeWitness).dumpSubtree(typeHierarchy))
    }
}


data class GenerateVisitorsTask(val outputDir: Path) : JjtxTask() {

    override fun execute(ctx: JjtxContext) {

        val globalCtx = VelocityContext()
        globalCtx["grammar"] = GrammarBean.create(ctx)
        globalCtx["global"] = ctx.jjtxOptsModel.templateContext
        globalCtx["H"] = "#"


        for ((id, visitor) in ctx.jjtxOptsModel.visitors) {

            if (!visitor.execute) {
                ctx.errorCollector.handleError(
                    "Visitor $id is not configured for execution",
                    ErrorCategory.VISITOR_NOT_RUN
                )
                continue
            }

            try {
                visitor.execute(ctx, globalCtx, outputDir)
            } catch (e: Exception) {
                // FIXME report cleanly
                e.printStackTrace()
            }
        }
    }
}
