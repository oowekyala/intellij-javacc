package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.JjtxContext
import com.google.common.io.Resources
import com.google.googlejavaformat.java.Formatter
import com.intellij.util.io.createFile
import com.intellij.util.io.exists
import com.intellij.util.io.isDirectory
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import java.nio.file.Path


// The field names of this class are public API, because they're serialized
// They're decoupled from the real VisitorConfig though
data class VisitorConfigBean(
    val templateFile: String?,
    val template: String?,
    val formatter: String? = "java",
    val output: String?,
    val context: Map<String, Any?>
) {


    fun toConfig(): VisitorConfig {
        return VisitorConfig(
            templateFile = templateFile,
            template = template,
            formatter = FormatterOpt.select(formatter),
            output = output,
            context = context
        )
    }
}

enum class FormatterOpt(private val doFormat: (String) -> String) {
    JAVA({ Formatter().formatSource(it) });


    fun format(source: String) = doFormat(source)

    companion object {
        fun select(key: String?): FormatterOpt? {
            val k = key?.toUpperCase() ?: return null
            return values().firstOrNull { it.name == k }
        }
    }
}

/**
 * @author Clément Fournier
 */
data class VisitorConfig(val templateFile: String?,
                         val template: String?,
                         val formatter: FormatterOpt?,
                         val output: String?,
                         val context: Map<String, Any?>) {


    private fun resolveTemplate(ctx: JjtxContext): String {

        return when {

            template != null && templateFile == null -> template

            template == null && templateFile != null -> {

                fun fromResource() = VisitorConfig::class.java.getResource(templateFile)?.let {
                    Resources.toString(it, Charsets.UTF_8)
                }

                fun fromFile() = ctx.grammarDir.resolve(templateFile).toFile().readText()

                fromResource() ?: fromFile()
            }


            else                                     -> throw java.lang.IllegalStateException(
                "Visitor spec must mention either 'templateFile' or 'template'"
            )
        }
    }

    private fun resolveOutput(velocityContext: VelocityContext,
                              outputDir: Path): Path {

        if (output == null) {
            throw java.lang.IllegalStateException("Visitor spec must mention the 'output' file")
        }

        val engine = VelocityEngine()

        val fname = StringWriter().also {
            engine.evaluate(velocityContext, it, "visitor-output", output)
        }.toString()


        val o = outputDir.resolve(fname)


        if (o.isDirectory()) {
            throw IllegalStateException("Output file ${this.output} is directory")
        }

        if (!o.exists()) {
            // todo log
            o.createFile()
        }

        return o
    }


    fun execute(ctx: JjtxContext, sharedCtx: VelocityContext, outputDir: Path) {

        val fullCtx = VelocityContext(context, sharedCtx)

        val template = resolveTemplate(ctx)
        val engine = VelocityEngine()

        val o = resolveOutput(fullCtx, outputDir)

        val rendered = StringWriter().also {
            engine.evaluate(fullCtx, it, "visitor-template", template)
        }.toString()

        afterRender(ctx, rendered, o)

    }

    private fun afterRender(ctx: JjtxContext, rendered: String, output: Path) {

        val formatted = formatter?.format(rendered) ?: rendered

        output.toFile().bufferedWriter().use {
            it.write(formatted)
        }
    }


}
