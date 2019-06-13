package com.github.oowekyala.jjtx.preprocessor

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.templates.*
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
    val implementNodeConstants: Boolean = true,

    /**
     * Cast the exceptions at run-time to force declaration of checked exceptions.
     * If you trust your own code, set it to false exceptions to throw exceptions
     * immediately.
     */
    val castExceptions: Boolean = true,

    val supportFiles: Map<String, FileGenTask> = emptyMap()
)

/**
 * Defaults correspond to full jjtree compatibility.
 */
data class JjtreeCompatBean(
    var implementNodeConstants: Boolean? = null,
    //    var dontCloseBeforeLastParserAction: Boolean = false,
    var forceCheckedExceptionsDeclaration: Boolean? = null,
    val supportFiles: Map<String, FileGenBean>? = null
)

fun JjtreeCompatBean.completeWith(parent: JjtreeCompatBean) = JjtreeCompatBean(
    implementNodeConstants = implementNodeConstants ?: parent.implementNodeConstants,
    forceCheckedExceptionsDeclaration = forceCheckedExceptionsDeclaration ?: parent.forceCheckedExceptionsDeclaration,
    supportFiles = supportFiles.completeWith(parent.supportFiles.orEmpty(), emptySet())
)

internal fun JjtreeCompatBean.toModel(ctx: JjtxContext): JavaccGenOptions = JavaccGenOptions(
    implementNodeConstants = implementNodeConstants ?: true,
    castExceptions = forceCheckedExceptionsDeclaration ?: true,
    supportFiles = supportFiles?.mapValuesNotNull { (id, fgb) ->
        fgb.toFileGen(ctx, positionInfo = null, id = id)
            ?.resolveStaticTemplates(ctx)
            ?.let {
                // handle visibility defaulting
                val dftVisibility =
                    if (ctx.jjtxOptsModel.inlineBindings.isPublicSupportClasses) "public" else null
                if ("visibility" !in it.context) it.copy(context = it.context + ("visibility" to dftVisibility))
                else it
            }
    } ?: emptyMap()
)

fun JavaccGenOptions.toBean() = JjtreeCompatBean(
    implementNodeConstants = implementNodeConstants,
    forceCheckedExceptionsDeclaration = castExceptions,
    supportFiles = supportFiles.mapValues { (_, v) -> v.toBean() }
)
