package com.github.oowekyala.ijcc.jjtx.visitors

import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.google.googlejavaformat.java.Formatter
import com.intellij.util.io.createFile
import com.intellij.util.io.exists
import com.intellij.util.io.isDirectory
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.File
import java.io.FileNotFoundException
import java.io.StringWriter
import java.nio.file.Path


data class VisitorConfigBean(
    val templateFile: String?,
    val template: String?,
    val output: String?,
    val context: Map<String, Any?>
)

/**
 * @author Clément Fournier
 */
data class VisitorConfig(val templateFile: String?,
                         val template: String?,
                         val output: String?,
                         val context: Map<String, Any?>) {


    private fun resolveTemplate(ctx: JjtxRunContext): String {

        return when {

            template != null && templateFile == null -> template

            template == null && templateFile != null -> {

                val file: File =
                    VisitorConfig::class.java.classLoader.getResource(templateFile)?.toExternalForm()?.let { File(it) }
                        ?: ctx.jjtxParams.grammarDir.resolve(templateFile).toFile()

                if (!file.exists()) throw FileNotFoundException("Cannot resolve template file $templateFile")

                file.readText()
            }


            else                                     -> throw java.lang.IllegalStateException(
                "Visitor spec must mention either 'templateFile' or 'template'"
            )
        }
    }

    private fun resolveOutput(ctx: JjtxRunContext,
                              velocityContext: VelocityContext): Path {

        if (output == null) {
            throw java.lang.IllegalStateException("Visitor spec must mention the 'output' file")
        }

        val engine = VelocityEngine()

        val fname = StringWriter().also {
            engine.evaluate(velocityContext, it, "visitor-output", output)
        }.toString()


        val o = ctx.jjtxParams.outputDir.resolve(fname)


        if (o.isDirectory()) {
            throw IllegalStateException("Output file ${this.output} is directory")
        }

        if (!o.exists()) {
            // todo log
            o.createFile()
        }

        return o
    }



    fun execute(ctx: JjtxRunContext) {

        val baseCtx = VelocityContext(context)

        baseCtx["grammar"] = GrammarBean.create(ctx)
        baseCtx["user"] = context


        val template = resolveTemplate(ctx)
        val engine = VelocityEngine()

        val o = resolveOutput(ctx, baseCtx)

        val rendered =StringWriter().also {
            engine.evaluate(baseCtx, it, "visitor-template", template)
        }.toString()

        afterRender(ctx, rendered, o)

    }

    private fun afterRender(ctx: JjtxRunContext, rendered: String, output: Path) {
        val formatted = Formatter().formatSource(rendered)
        output.toFile().bufferedWriter().use {
            it.write(formatted)
        }
    }


}
