package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.preprocessor.VanillaJjtreeBuilder
import com.github.oowekyala.jjtx.preprocessor.toJavacc
import com.github.oowekyala.jjtx.reporting.reportException
import com.github.oowekyala.jjtx.reporting.reportNormal
import com.github.oowekyala.jjtx.reporting.reportSyntaxErrors
import com.github.oowekyala.jjtx.util.createFile
import com.github.oowekyala.jjtx.util.exists
import java.io.FileOutputStream
import java.io.IOException

/**
 * Compile the grammar to a JavaCC file.
 */
class GenerateJavaccTask(private val taskCtx: TaskCtx) : JjtxTask() {


    override fun execute() {

        taskCtx.run {

            val invalidSyntax = ctx.grammarFile.reportSyntaxErrors(ctx)

            if (invalidSyntax) {
                return
            }

            val o = ctx.genJjPath(outputDir)

            if (!o.exists()) {
                o.createFile()
            }

            val opts = ctx.jjtxOptsModel.javaccGen

            try {

                val builder =
                    VanillaJjtreeBuilder(ctx.grammarFile.grammarOptions, opts)

                FileOutputStream(o.toFile()).buffered()
                    .use {
                        toJavacc(input = ctx.grammarFile, out = it, options = opts, builder = builder, vcontext = ctx.globalVelocityContext)
                    }

                ctx.messageCollector.reportNormal("Generated JavaCC grammar $o")

            } catch (ioe: IOException) {
                ctx.messageCollector.reportException(ioe, contextStr = "Generating JavaCC file", fatal = false)
            }
        }
    }
}
