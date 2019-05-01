package com.github.oowekyala.jjtx.typeHierarchy

import com.github.oowekyala.jjtx.ErrorCollector.Category.DUPLICATE_MATCH
import com.github.oowekyala.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.util.asMap
import com.github.oowekyala.ijcc.util.associateByToMostlySingular
import com.github.oowekyala.jjtx.JjtxContext


fun TypeHierarchyTree.removeDuplicates(ctx: JjtxContext): TypeHierarchyTree {

    val copy = deepCopy()

    val multi = copy.descendants().associateByToMostlySingular { it.nodeName }

    for ((name, dups) in multi.asMap()) {
        if (dups.size > 1) {
            val mostSpecific = dups.maxBy { it.specificity.ordinal }!!

            ctx.errorCollector.handleError(
                "Node $name is matched by several productions, selecting ${mostSpecific.positionInfo} by specificity",
                DUPLICATE_MATCH,
                sourcePosition = *dups.map { it.positionInfo }.toTypedArray()
            )

            for (n in dups) {
                if (n !== mostSpecific) {
                    n.removeFromParent() // TODO could be the root!!
                }
            }
        }
    }

    return copy
}
