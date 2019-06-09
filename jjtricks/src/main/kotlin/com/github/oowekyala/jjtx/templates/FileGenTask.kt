package com.github.oowekyala.jjtx.templates

import com.github.oowekyala.ijcc.util.deleteWhitespace
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.reporting.JjtricksExceptionWrapper
import com.github.oowekyala.jjtx.reporting.MessageCategory
import com.github.oowekyala.jjtx.reporting.report
import com.github.oowekyala.jjtx.reporting.reportWrappedException
import com.github.oowekyala.jjtx.util.*
import com.github.oowekyala.jjtx.util.io.StringSource
import com.github.oowekyala.jjtx.util.io.readText
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path


/**
 * Generation task for a single file from a velocity template.
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
open class FileGenTask internal constructor(
    val template: StringSource,
    val formatter: SourceFormatter?,
    val genFqcn: String,
    /**
     * Local bindings.
     */
    val context: Map<String, Any?>
) {

    /**
     * Evaluate the static templates of the options file to
     * produce a potentially runnable task.
     */
    fun resolveStaticTemplates(ctx: JjtxContext): FileGenTask {
        val engine = VelocityEngine()
        val staticCtx = ctx.initialVelocityContext + context

        return FileGenTask(
            context = context,
            formatter = formatter,
            genFqcn = engine.evaluate(staticCtx, genFqcn).let(::recogniseQname),
            template = template // TODO template the file?
        )
    }



    /**
     * Returns a pair (fqcn, path) of the FQCN of the generated class
     * and the path where the file should be put in the [outputDir].
     * The path may not exist.
     */
    private fun resolveOutput(genFqcn: String,
                              outputDir: Path): Pair<String, Path> {

        val fqcn = recogniseQname(genFqcn)
        if (!fqcn.matches(StrictFqcnRegex)) {
            throw RuntimeException("'genClassName' should be a fully qualified class name, but was $genFqcn")
        }

        val o: Path = outputDir.resolve(fqcn.replace('.', '/') + ".java").toAbsolutePath()

        if (o.isDirectory()) {
            throw IOException("Output file $o is a directory")
        }

        return Pair(genFqcn, o)
    }


    private fun resolveTemplate(ctx: JjtxContext, template: StringSource): String {

        return when (template) {
            is StringSource.Str  -> template.source
            is StringSource.File -> {

                val nis = ctx.resolveResource(template.fname)
                    ?: throw FileNotFoundException("File not found ${template.fname}")

                return nis.readText()
            }
        }
    }


    /**
     * Executes the generation.
     *
     * @param [ctx] Run context
     * @param [sharedCtx] Global velocity context, the local properties will be chained
     * @param [outputDir] Root directory where the visitors should be generated
     */
    open fun execute(ctx: JjtxContext,
                     sharedCtx: VelocityContext,
                     outputDir: Path,
                     otherSourceRoots: List<Path>): Triple<Status, String, Path> {

        val (fqcn, o) = resolveOutput(genFqcn, outputDir)

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


        if (!o.exists()) {
            o.createFile()
        }

        val fullCtx =
            sharedCtx + context + mapOf(
                "thisClass" to ClassVBean(qualifiedName = fqcn),
                "thisFile" to FileVBean(absolutePath = o),
                "timestamp" to ctx.io.now()
            )

        val template = resolveTemplate(ctx, template)


        val rendered = VelocityEngine().evaluate(fullCtx, logId = o.toString(), template = template) {
            throw JjtricksExceptionWrapper.withKnownFileCtx(it, template, o)
        }


        val formatted = try {
            formatter?.format(rendered)
        } catch (e: Exception) {

            val wrapper = JjtricksExceptionWrapper.withKnownFileCtx(e, rendered, o)

            ctx.messageCollector.reportWrappedException(
                wrapper = wrapper,
                contextStr = "applying formatter '${formatter!!.name.toLowerCase()}' on $o"
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

    companion object {
        private val StrictFqcnRegex = Regex("([A-Za-z_][\\w\$]*)(\\.[A-Za-z_][\\w\$]*)*")

        // somewhat lenient to catch stupid empty package errors
        private fun recogniseQname(fqcn: String): String =
            fqcn.deleteWhitespace().removePrefix(".").replace(Regex("\\.+"), ".")

    }
}


enum class Status {
    Generated,
    Aborted
}
