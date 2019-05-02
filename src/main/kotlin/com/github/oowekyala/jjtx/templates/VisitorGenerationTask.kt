package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
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
    val execute: Boolean?,
    val templateFile: String?,
    val template: String?,
    val formatter: String?,
    val output: String?,
    val context: Map<String, Any?>?
) {

    /**
     * Completes the missing settings of this bean with those of the [other] bean.
     * Beans are merged with beans with the same id higher up the config chain. If
     * in the end, the merged bean should [execute], then validation is performed by
     * [toConfig] and the bean is promoted to a complete [VisitorGenerationTask].
     */
    fun merge(other: VisitorConfigBean): VisitorConfigBean =
        VisitorConfigBean(
            execute = execute ?: other.execute,
            templateFile = templateFile ?: other.templateFile,
            template = template ?: other.template,
            formatter = formatter ?: other.formatter,
            output = output ?: other.output,
            // merge the contexts
            context = (other.context ?: emptyMap()).plus(this.context ?: emptyMap())
        )


    fun toConfig(id: String): VisitorGenerationTask? {

        if (execute == false) {
            // the config is not even checked
            return null
        }

        val t = if (templateFile == null && template == null) {
            throw java.lang.IllegalStateException("Visitor spec '$id' must mention either 'templateFile' or 'template'")
        } else if (template != null) {
            TemplateSource.Source(template)
        } else {
            TemplateSource.File(templateFile!!)
        }

        if (output == null) {
            throw java.lang.IllegalStateException("Visitor spec '$id' must mention the 'output' file")
        }

        return VisitorGenerationTask(
            execute = execute ?: true,
            template = t,
            formatter = FormatterOpt.select(formatter ?: "java"),
            outputFileName = output,
            context = context ?: emptyMap()
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

sealed class TemplateSource {

    data class File(val fname: String) : TemplateSource()
    data class Source(val source: String) : TemplateSource()

}

/**
 * Gathers the info required by a visitor generation task.
 *
 * Visitor generation generates a single file using a velocity
 * template.
 *
 * The velocity context is build as follows:
 *
 * - The [GrammarBean] is put under key "grammar". This provides
 * access to the full type hierarchy, among other things.
 * - The user can add their own variables shared by all visitor
 * runs by using the "jjtx.templateContext" key in the [JjtxOptsModel].
 * This is put under the key "global". Those are chained following
 * the option file config chain.
 * - Visitor-specific mappings, specified in visitor's [context]
 * element, are put directly into the inner context.
 *
 * @author Clément Fournier
 */
data class VisitorGenerationTask(
    val execute: Boolean,
    val template: TemplateSource,
    val formatter: FormatterOpt?,
    val outputFileName: String,
    val context: Map<String, Any?>
) {


    private fun resolveTemplate(ctx: JjtxContext): String {

        return when (template) {

            is TemplateSource.Source -> template.source

            is TemplateSource.File   -> {

                fun fromResource() = VisitorGenerationTask::class.java.getResource(template.fname)?.let {
                    Resources.toString(it, Charsets.UTF_8)
                }

                fun fromFile() = ctx.grammarDir.resolve(template.fname).toFile().readText()

                fromResource() ?: fromFile()
            }
        }
    }

    private fun resolveOutput(velocityContext: VelocityContext,
                              outputDir: Path): Path {

        val engine = VelocityEngine()

        val fname = StringWriter().also {
            engine.evaluate(velocityContext, it, "visitor-output", outputFileName)
        }.toString()


        val o = outputDir.resolve(fname)


        if (o.isDirectory()) {
            throw IllegalStateException("Output file ${this.outputFileName} is directory")
        }

        if (!o.exists()) {
            // todo log
            o.createFile()
        }

        return o
    }

    /**
     * Executes the visitor run.
     *
     * @param [ctx] Run context
     * @param [sharedCtx] Global velocity context, the local properties will be chained
     * @param [outputDir] Root directory where the visitors should be generated
     */
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

        val formatted =
            try {
                formatter?.format(rendered)
            } catch (e: Exception) {
                null
            } ?: rendered

        output.toFile().bufferedWriter().use {
            it.write(formatted)
        }
    }


}
