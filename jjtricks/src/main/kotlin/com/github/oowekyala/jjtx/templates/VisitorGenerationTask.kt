package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.util.StringSource
import org.apache.velocity.VelocityContext
import java.nio.file.Path


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
            StringSource.Str(template)
        } else {
            StringSource.File(templateFile!!)
        }

        if (genClassName == null) {
            throw java.lang.IllegalStateException("Visitor spec '$id' must mention 'genClassName', the template for the fully qualified class name of the generated class")
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
 * Gathers the info required by a visitor generation task.
 *
 * Visitor generation generates a single file using a velocity
 * template.
 *
 * The velocity context is build as follows:
 *
 * - The [GrammarVBean] is put under key "grammar". This provides
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
class VisitorGenerationTask internal constructor(
    private val myBean: VisitorConfigBean,
    val id: String,
    val execute: Boolean,
    template: StringSource,
    formatter: FormatterChoice?,
    genFqcn: String,
    context: Map<String, Any?>
) : FileGenTask(template, formatter, genFqcn, context) {

    override fun execute(ctx: JjtxContext,
                         sharedCtx: VelocityContext,
                         outputDir: Path,
                         otherSourceRoots: List<Path>): Triple<Status, String, Path> {
        val ret = super.execute(ctx, sharedCtx, outputDir, otherSourceRoots)
        myBean.genClassName = ret.second
        return ret
    }
}

