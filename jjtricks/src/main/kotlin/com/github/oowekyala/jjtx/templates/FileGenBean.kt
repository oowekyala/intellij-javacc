package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.reporting.reportNonFatal
import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.jjtx.util.io.StringSource
import java.util.*


// The field names of this class are public API, because they're serialized
// They're decoupled from the real VisitorConfig though
/**
 * Configuration of a visitor generation task, as input under `jjtx.visitors`.
 * `jjtx.visitors` is a map of ids to [FileGenBean]. If a configuration
 * with the same id is found in the parent [JjtxOptsModel], the child configuration
 * is completed by the parent configuration.
 *
 * @property templateFile The path to a file containing the template. Can be a classpath resource.
 * @property template The source of a template, if present, overrides [templateFile]
 * @property formatter The name of a formatter to use, available formatters are listed in [FormatterRegistry]
 * @property genClassName A template evaluating to the FQCN of the class to generate.
 * @property context A map of additional context variables available in the template
 */
data class FileGenBean(
    val templateFile: String?,
    val template: String?,
    val formatter: String?,
    var genClassName: String?,
    val context: Map<String, Any?>?
) {


    private fun getTemplate(ctx: JjtxContext, positionInfo: Position?): StringSource? = when {
        template != null     -> StringSource.Str(template)
        templateFile != null -> StringSource.File(templateFile)
        else                 -> {
            ctx.messageCollector.reportNonFatal(
                "File generation task must mention either 'templateFile' or 'template'",
                positionInfo
            )
            null
        }
    }

    fun toNodeGenScheme(ctx: JjtxContext, positionInfo: Position?, nodeBeans: List<NodeVBean>): NodeGenerationScheme? {

        val t = getTemplate(ctx, positionInfo) ?: return null

        return NodeGenerationScheme(
            nodeBeans = nodeBeans,
            genClassTemplate = genClassName,
            template = t,
            context = context ?: emptyMap(),
            formatter = FormatterRegistry.getOrDefault(formatter)
        )

    }

    /**
     * Creates a runnable [FileGenTask] from this configuration,
     * validating the parameters.
     *
     * @return A generation task if all parameters are valid. If [execute] is
     *  false, returns null.
     *
     * @throws IllegalStateException if this config is invalid
     */
    fun toFileGen(ctx: JjtxContext, positionInfo: Position?, id: String): FileGenTask? {

        val t = getTemplate(ctx, positionInfo) ?: return null

        if (genClassName == null) {
            ctx.messageCollector.reportNonFatal("File generation task '$id' must mention 'genClassName', the template for the fully qualified class name of the generated class")
            return null
        }

        return FileGenTask(
            template = t,
            formatter = FormatterRegistry.getOrDefault(formatter),
            genFqcn = genClassName!!,
            context = context ?: emptyMap()
        )
    }

}

fun FileGenTask.toBean() =
    FileGenBean(
        templateFile = (template as? StringSource.File)?.fname,
        template = (template as? StringSource.Str)?.source,
        genClassName = genFqcn,
        formatter = formatter?.name?.toLowerCase(Locale.ROOT),
        context = context
    )
