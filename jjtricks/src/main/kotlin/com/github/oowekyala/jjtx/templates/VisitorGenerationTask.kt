package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.util.*
import com.google.common.io.Resources
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import java.nio.file.Path
import java.util.*


// The field names of this class are public API, because they're serialized
// They're decoupled from the real VisitorConfig though
/**
 * Configuration of a visitor generation task, as input under `jjtx.visitors`.
 * `jjtx.visitors` is a map of ids to [VisitorConfigBean]. If a configuration
 * with the same id is found in the parent [JjtxOptsModel], the child configuration
 * is completed by the parent configuration.
 *
 * @property execute Whether the task will be run by JJTricks
 * @property templateFile The path to a file containing the template. Can be a classpath resource.
 * @property template The source of a template, if present, overrides [templateFile]
 * @property formatter The name of a formatter to use, available formatters are listed in [FormatterChoice]
 * @property genClassName A template evaluating to the FQCN of the class to generate.
 * @property context A map of additional context variables available in the template
 */
data class VisitorConfigBean(
    val execute: Boolean?,
    val templateFile: String?,
    val template: String?,
    val formatter: String?,
    var genClassName: String?,
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
            genClassName = genClassName ?: other.genClassName,
            // merge the contexts
            context = (other.context ?: emptyMap()).plus(this.context ?: emptyMap())
        )


    /**
     * Creates a runnable [VisitorGenerationTask] from this configuration,
     * validating the parameters.
     *
     * @return A generation task if all parameters are valid. If [execute] is
     *  false, returns null.
     *
     * @throws IllegalStateException if this config is invalid
     */
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

        if (genClassName == null) {
            throw java.lang.IllegalStateException("Visitor spec '$id' must mention 'genClassName', the a fully qualified class name of the generated class")
        }

        return VisitorGenerationTask(
            myBean = this,
            id = id,
            execute = execute ?: true,
            template = t,
            formatter = FormatterChoice.select(formatter ?: "java"),
            genFqcn = genClassName!!,
            context = context ?: emptyMap()
        )
    }
}

/**
 * Type of source for a [VisitorGenerationTask].
 */
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
data class VisitorGenerationTask internal constructor(
    private val myBean: VisitorConfigBean,
    val id: String,
    val execute: Boolean,
    val template: TemplateSource,
    val formatter: FormatterChoice?,
    val genFqcn: String,
    val context: Map<String, Any?>
) {


    private fun resolveTemplate(ctx: JjtxContext): String {

        return when (template) {

            is TemplateSource.Source -> template.source

            is TemplateSource.File   -> {

                fun fromResource() = Jjtricks.getResource(template.fname)?.let {
                    Resources.toString(it, Charsets.UTF_8)
                }

                fun fromFile() = ctx.grammarDir.resolve(template.fname).toFile().readText()

                fromResource() ?: fromFile()
            }
        }
    }

    /**
     * Returns a pair (fqcn, path) of the FQCN of the generated class
     * and the path where the file should be put in the [outputDir].
     */
    private fun resolveOutput(velocityContext: VelocityContext,
                              outputDir: Path): Pair<String, Path> {

        val engine = VelocityEngine()

        val templated = StringWriter().also {
            engine.evaluate(velocityContext, it, "visitor-output", genFqcn)
        }.toString()


        val fqcnRegex = Regex("([A-Za-z_][\\w\$]*)(\\.[A-Za-z_][\\w\$]*)*")
        if (!templated.matches(fqcnRegex)) {
            throw java.lang.IllegalStateException("'genClassName' should be a fully qualified class name, but was $templated")
        }

        val o: Path = outputDir.resolve(templated.replace('.', '/') + ".java")

        if (o.isDirectory()) {
            throw IllegalStateException("Output file ${this.genFqcn} is directory")
        }

        if (!o.exists()) {
            // todo log
            o.createFile()
        }

        // The beans are dumped, so we update it with the final value
        myBean.genClassName = templated

        return Pair(templated, o)
    }

    private fun withLocalBindings(sharedCtx: VelocityContext,
                                  vararg additionalBindings: Pair<String, Any>): VelocityContext {

        val local = VelocityContext(additionalBindings.toMap(), sharedCtx)

        return VelocityContext(context, local)
    }

    /**
     * Executes the visitor run.
     *
     * @param [ctx] Run context
     * @param [sharedCtx] Global velocity context, the local properties will be chained
     * @param [outputDir] Root directory where the visitors should be generated
     */
    fun execute(ctx: JjtxContext, sharedCtx: VelocityContext, outputDir: Path) {

        val tmpCtx = withLocalBindings(sharedCtx)

        val template = resolveTemplate(ctx)
        val engine = VelocityEngine()

        val (fqcn, o) = resolveOutput(tmpCtx, outputDir)

        val (pack, simpleName) = fqcn.splitAroundLast('.')

        val fullCtx =
            withLocalBindings(
                sharedCtx,
                "package" to pack,
                "simpleName" to simpleName,
                "timestamp" to Date()
            )

        val rendered = StringWriter().also {
            engine.evaluate(fullCtx, it, id, template)
        }.toString()


        val formatted = try {
            formatter?.format(rendered)
        } catch (e: Exception) {
            ctx.messageCollector.report(
                "Exception applying formatter '${formatter!!.name.toLowerCase()}': ${e.message}",
                ErrorCategory.FORMATTER_ERROR
            )
            null
        } ?: rendered

        o.toFile().bufferedWriter().use {
            it.write(formatted)
        }

        ctx.messageCollector.report("Generated visitor '$id' into $o", ErrorCategory.VISITOR_GENERATED)
    }


}
