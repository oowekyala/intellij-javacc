package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.jjtx.Jjtricks
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.util.*
import com.google.common.io.Resources
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.nio.file.Path
import java.util.*


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
open class FileGenTask internal constructor(
    val template: TemplateSource,
    private val formatter: FormatterChoice?,
    private val genFqcn: String,
    val context: Map<String, Any?>
) {


    /**
     * Returns a pair (fqcn, path) of the FQCN of the generated class
     * and the path where the file should be put in the [outputDir].
     */
    private fun resolveOutput(ctx: JjtxContext,
                              genFqcn: String,
                              velocityContext: VelocityContext,
                              outputDir: Path): Pair<String, Path> {

        val engine = VelocityEngine()

        val templated = engine.evaluate(velocityContext, genFqcn)

        // TODO be more lenient
        val fqcnRegex = Regex("([A-Za-z_][\\w\$]*)(\\.[A-Za-z_][\\w\$]*)*")
        if (!templated.matches(fqcnRegex)) {
            ctx.messageCollector.reportError("'genClassName' should be a fully qualified class name, but was $templated")
        }

        val o: Path = outputDir.resolve(templated.replace('.', '/') + ".java").toAbsolutePath()

        if (o.isDirectory()) {
            ctx.messageCollector.reportError("Output file $templated is directory")
        }

        if (!o.exists()) {
            o.createFile()
        }

        return Pair(templated, o)
    }


    private fun resolveTemplate(ctx: JjtxContext, template: TemplateSource): String {

        return when (template) {
            is TemplateSource.Source -> template.source
            is TemplateSource.File   -> {


                fun fromResource(): String? = Jjtricks.getResource(template.fname)?.let {
                    Resources.toString(it, Charsets.UTF_8)
                }

                fun fromFile(): String {

                    val file = ctx.grammarDir.resolve(template.fname).toFile()

                    if (!file.isFile) {
                        ctx.messageCollector.reportError("File not found $file")
                    }

                    return file.readText()
                }

                return fromResource() ?: fromFile()
            }
        }


    }

    private fun withLocalBindings(context: Map<String, Any?>,
                                  sharedCtx: VelocityContext,
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
    open fun execute(ctx: JjtxContext,
                     sharedCtx: VelocityContext,
                     outputDir: Path,
                     otherSourceRoots: List<Path>): Triple<Status, String, Path> {

        val tmpCtx = withLocalBindings(context, sharedCtx)

        val template = resolveTemplate(ctx, template)
        val engine = VelocityEngine()

        val (fqcn, o) = resolveOutput(ctx, genFqcn, tmpCtx, outputDir)

        val rel = outputDir.relativize(o)

        for (root in otherSourceRoots) {
            if (root.resolve(rel).exists()) {
                ctx.messageCollector.report(
                    "Class $fqcn was not generated because present in $root",
                    MessageCategory.CLASS_NOT_GENERATED
                )
                return Triple(Status.Aborted, fqcn, o)
            }
        }

        val (pack, simpleName) = fqcn.splitAroundLast('.')

        val fullCtx =
            withLocalBindings(
                context,
                sharedCtx,
                "package" to pack,
                "simpleName" to simpleName,
                "timestamp" to ctx.io.now()
            )

        val rendered = engine.evaluate(fullCtx, template)


        val formatted = try {
            formatter?.format(rendered)
        } catch (e: Exception) {
            ctx.messageCollector.report(
                "Exception applying formatter '${formatter!!.name.toLowerCase()}': ${e.message}",
                MessageCategory.FORMATTER_ERROR
            )
            null
        } ?: rendered

        o.toFile().bufferedWriter().use {
            it.write(formatted)
        }

        ctx.messageCollector.report(
            "Class $fqcn was generated in $o",
            MessageCategory.CLASS_GENERATED
        )

        return Triple(Status.Generated, fqcn, o)
    }

}


enum class Status {
    Generated,
    Aborted
}
