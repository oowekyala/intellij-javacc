package com.github.oowekyala.ijcc.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Category.UNCOVERED_NODE
import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Severity.FAIL
import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Severity.INFO
import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.jjtx.position
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner


fun TypeHierarchyTree.adoptOrphansOnRoot(names: Iterable<JjtNodeClassOwner>, ctx: JjtxRunContext): TypeHierarchyTree {

    val remaining = names.groupByTo(mutableMapOf()) { it.nodeQualifiedName }

    remaining.remove(null)

    descendants().forEach { remaining.remove(it.nodeName) }

    if (remaining.isNotEmpty()) {

        val remainingShortNames =
            remaining.values.flatMap { it }.map { it.nodeRawName }.distinct()

        val severity = ctx.errorCollector.handleError("${remainingShortNames.size} nodes are not mentioned in the type hierarchy",
            UNCOVERED_NODE
        )

        if (severity < FAIL) {

            ctx.errorCollector.handleError(
                "They will be adopted by the root (${this.nodeName})",
                UNCOVERED_NODE,
                severityOverride = INFO
            )

            val newChildren = children + remaining.entries.map { (qname, owners) ->
                TypeHierarchyTree(qname!!, owners[0].position(), emptyList(), Specificity.UNKNOWN)
            }

            return TypeHierarchyTree(nodeName, positionInfo, newChildren, specificity)
        }
    }

    return this
}