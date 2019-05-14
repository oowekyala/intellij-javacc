package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.ijcc.util.init
import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.reporting.reportFatal
import com.github.oowekyala.jjtx.util.JsonPointer
import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.jjtx.util.dataAst.ScalarType.REFERENCE
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.github.oowekyala.jjtx.util.io.ResourceResolver
import java.io.Closeable


private class TreeWalkHelper(val resolver: ResourceResolver<NamedInputStream>,
                             private val err: MessageCollector) : Closeable {

    private data class CrossTreeRef(
        val position: Position,
        val source: ResolvedTreeRef,
        val target: ResolvedTreeRef
    )

    private data class ResolvedTreeRef(
        val tree: NamedInputStream,
        val pointer: JsonPointer
    )

    private val processedTrees = mutableMapOf<NamedInputStream, DataAstNode>()

    fun parseInclusionTree(nis: NamedInputStream): DataAstNode = nis.getResolvedTree(listOf(), listOf(nis))


    private fun NamedInputStream.getResolvedTree(inclusionPosPath: List<Position>,
                                                 inclusionPath: List<NamedInputStream>): DataAstNode {

        if (this in inclusionPath.init()) {
            err.reportFatal("Cycle in file inclusions", *inclusionPosPath.toTypedArray())
        } else {
            return processedTrees.computeIfAbsent(this) {
                it.buildResolvedTree(inclusionPosPath, inclusionPath)
            }
        }

    }

    private fun NamedInputStream.buildResolvedTree(inclusionPosPath: List<Position>,
                                                   inclusionPath: List<NamedInputStream>): DataAstNode {


        val root = parseGuessFromExtension(this)

        val refs = mutableSetOf<CrossTreeRef>()
        var newT = root.collectRefs(this, refs)

        val todo = refs.groupBy { it.target.tree }

        for ((next, targets) in todo) {
            val nextResolved = next.getResolvedTree(inclusionPosPath + targets.first().position, inclusionPath + this)

            newT = newT.rebuildPointerSet(nextResolved, targets)
        }

        return newT
    }

    private fun DataAstNode.rebuildPointerSet(targetTree: DataAstNode, refs: List<CrossTreeRef>): DataAstNode {

        // This could be optimised to a prefix tree
        val pointers = refs.groupByTo(mutableMapOf()) { it.source.pointer }


        return rebuild { p ->
            if (this is AstScalar && type == REFERENCE && p in pointers) {
                val r = this.typedValue as TreeRef
                r.jsonPointer.findIn(targetTree) ?: err.reportFatal("Element ${r.jsonPointer} missing in ${r.resource}")
            } else {
                this
            }
        }

    }


    private fun DataAstNode.collectRefs(myOrigin: NamedInputStream,
                                        refs: MutableSet<CrossTreeRef>): DataAstNode {


        val root = this
        return rebuild { pointer ->
            if (this is AstScalar && type == REFERENCE) {
                val target = typedValue as TreeRef

                if (target.resource.isEmpty()) {
                    // reference to this file
                    if (pointer in target.jsonPointer) {
                        // No detection across file boundaries, but inclusion cycles are prohibited
                        err.reportFatal("Reference $any includes itself recursively", position)
                    }
                    return@rebuild root.findPointer(target.jsonPointer)
                        ?: err.reportFatal("Element ${target.jsonPointer} missing in ${myOrigin.filename}", position)

                } else {
                    // fail early if a resource is not resolved
                    val resolved = resolver.getResource(target.resource)
                        ?: err.reportFatal("Unresolved reference ${target.resource}", position)

                    refs += CrossTreeRef(
                        position ?: pointer, // FIXME
                        ResolvedTreeRef(myOrigin, pointer),
                        ResolvedTreeRef(resolved, target.jsonPointer)
                    )
                }
            }

            this
        }
    }


    override fun close() {
        processedTrees.clear()
    }
}


/**
 * Parses the input stream and resolves the include directives.
 * Cyclic dependencies between files are prohibited, we don't
 * check if the particular subtrees are independent.
 *
 *
 */
fun NamedInputStream.parseAndResolveIncludes(
    resolver: ResourceResolver<NamedInputStream>,
    err: MessageCollector
): DataAstNode =
    TreeWalkHelper(resolver, err).use {
        it.parseInclusionTree(this)
    }

enum class VisitResult {
    CONTINUE, STOP_RECURSION, ABORT
}

fun DataAstNode.walkWithPointer(f: DataAstNode.(JsonPointer) -> VisitResult) {

    class StopError : Error()

    fun DataAstNode.walkHelper(myPointer: JsonPointer, f: DataAstNode.(JsonPointer) -> VisitResult) {

        when (this) {
            is AstScalar ->
                when (f(myPointer)) {
                    VisitResult.ABORT -> throw StopError()
                    else              -> {
                        /*do nothing*/
                    }
                }
            is AstMap    -> {
                when (f(myPointer)) {
                    VisitResult.ABORT    -> throw StopError()
                    VisitResult.CONTINUE ->
                        for ((k, v) in map) {
                            v.walkHelper(myPointer / k, f)
                        }


                    else                 -> {
                        /*do nothing*/
                    }
                }
            }
            is AstSeq    -> {
                when (f(myPointer)) {
                    VisitResult.ABORT    -> throw StopError()
                    VisitResult.CONTINUE ->
                        list.forEachIndexed { i, v ->
                            v.walkHelper(myPointer / i, f)
                        }


                    else                 -> {
                        /*do nothing*/
                    }
                }
            }
        }
    }

    try {
        walkHelper(JsonPointer.Root, f)
    } catch (e: StopError) {
        // return
    }

}


fun DataAstNode.rebuild(f: DataAstNode.(JsonPointer) -> DataAstNode): DataAstNode {

    fun DataAstNode.walkHelper(myPointer: JsonPointer, f: DataAstNode.(JsonPointer) -> DataAstNode): DataAstNode {

        return when (this) {
            is AstScalar -> f(myPointer)
            is AstMap    -> copy(map = map.mapValues { (k, v) -> v.walkHelper(myPointer / k, f) })
            is AstSeq    -> copy(list = list.mapIndexed { i, v -> v.walkHelper(myPointer / i, f) })

        }
    }
    return walkHelper(JsonPointer.Root, f)
}
