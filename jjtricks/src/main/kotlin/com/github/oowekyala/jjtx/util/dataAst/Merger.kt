package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.reporting.reportFatal
import com.github.oowekyala.jjtx.util.JsonPosition
import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.jjtx.util.io.CachedResourceResolver
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.github.oowekyala.jjtx.util.io.ResourceResolver


fun NamedInputStream.parseAndResolveIncludes(resolver: ResourceResolver, err: MessageCollector): DataAstNode =
    CachedResourceResolver(resolver).let {
        val parsed = parseAndResolveIncludes(listOf(), listOf(), it, err)
        it.drop() // drop the cache
        parsed
    }


private fun NamedInputStream.parseAndResolveIncludes(
    inclusionPosPath: List<Position>,
    inclusionPath: List<NamedInputStream>,
    resolver: ResourceResolver,
    err: MessageCollector
): DataAstNode {

    if (this in inclusionPath) {
        err.reportFatal("Cycle in file inclusions", *inclusionPosPath.toTypedArray())
    }

    val ast = parseGuessFromExtension(this)

    return ast.rebuildResolvingIncludes(
        JsonPosition.Root,
        inclusionPosPath,
        inclusionPath,
        resolver,
        err
    )

}

// TODO make tail-recursive at least
private fun DataAstNode.rebuildResolvingIncludes(
    myPointer: JsonPosition,
    inclusionPosPath: List<Position>,
    inclusionPath: List<NamedInputStream>,
    resolver: ResourceResolver,
    err: MessageCollector
): DataAstNode {

    return when (this) {
        is AstMap -> copy(
            map = map.mapValues { (k, v) ->
                v.rebuildResolvingIncludes(
                    myPointer.resolve(k),
                    inclusionPosPath,
                    inclusionPath,
                    resolver,
                    err
                )
            }
        )
        is AstSeq -> copy(
            list = list.mapIndexed { i, v ->
                v.rebuildResolvingIncludes(
                    myPointer.resolve(i.toString()),
                    inclusionPosPath,
                    inclusionPath,
                    resolver,
                    err
                )
            }
        )
        is AstScalar -> when (type) {
            ScalarType.REFERENCE -> {
                val (file, pointerInRes) = parseReference(any)

                if (file.isEmpty()) {
                    // reference to this file
                    if (myPointer in pointerInRes) {
                        // Todo detection across file boundaries? how can this go wrong?
                        err.reportFatal("Reference $any includes itself recursively", position)
                    } else {
                        // Resolved last
                        return this
                    }
                }


                val resolved = resolver.getStreamable(file) ?: err.reportFatal("Unresolved reference $file", position)

                resolved.parseAndResolveIncludes(
                    inclusionPosPath + position!!,
                    inclusionPath + resolved,
                    resolver,
                    err
                ).findPointer(pointerInRes) ?: err.reportFatal("Unresolved element $pointerInRes in $file", position)
            }
            else                 -> this
        }
    }
}

