package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.postprocessor.SpecialTemplate
import com.github.oowekyala.jjtx.reporting.reportNonFatal
import com.github.oowekyala.jjtx.templates.FileGenBean
import com.github.oowekyala.jjtx.templates.FileGenTask
import com.github.oowekyala.jjtx.templates.toBean
import com.github.oowekyala.jjtx.templates.toFileGen
import com.github.oowekyala.jjtx.util.mapValuesNotNull

/**
 * Compatibility options for JJTree.
 */
data class JavaccGenOptions(
    /**
     * Don't close the node scope before the last parser actions unit
     * in a scoped expansion unit. For example:
     *
     *     (Foo() { a } { b }) #Node
     *
     * JJTree inserts the closing code between `a` and `b`, which can
     * be confusing behaviour, since the stack isn't the same in `a`
     * and `b`.
     *
     * When set to true, this behaviour is changed and the node scope
     * is closed after `{b}`. This doesn't affect the scopes of productions,
     * since the last parser actions can be used to return `jjtThis`.
     */
    val dontCloseBeforeLastParserAction: Boolean = false,

    /**
     * If set to true, the parser will implement the interface containing
     * the constants. This is a code smell and is kept only for compatibility
     * with Jjtree.
     */
    val implementsList: List<String> = emptyList(),

    /**
     * Cast the exceptions at run-time to force declaration of checked exceptions.
     * This kind-of obfuscates the parser and the jj file. If you trust your own
     * code, set it to false to throw exceptions immediately.
     */
    val castExceptions: Boolean = true,

    val supportFiles: Map<String, FileGenTask> = emptyMap()
)

internal fun Map<String, FileGenBean>?.toModel(ctx: JjtxContext): JavaccGenOptions =
    if (this == null) JavaccGenOptions()
    else JavaccGenOptions(
        castExceptions = this[SpecialTemplate.JJ_FILE.id]?.context?.get("forceCheckedExceptionDeclaration")?.let { cast ->
            if (cast !is Boolean) {
                ctx.messageCollector.reportNonFatal("Expected 'forceCheckedExceptionDeclaration' to be a boolean")
                true
            } else cast
        } ?: true,
        implementsList = this[SpecialTemplate.JJ_FILE.id]?.context?.get("implements")?.let { impl ->
            if (impl !is Collection<*> || impl.any { it !is String }) {
                ctx.messageCollector.reportNonFatal("Expected 'implements' to be a collection of strings")
                emptyList()
            } else impl.map { it.toString() }
        } ?: emptyList(),
        supportFiles = this.mapValuesNotNull { (id, fgb) ->
            fgb.toFileGen(ctx, positionInfo = null, id = id)?.resolveStaticTemplates(ctx)
        }
    )

fun JavaccGenOptions.toBean() = supportFiles.mapValues { (_, v) -> v.toBean() }

