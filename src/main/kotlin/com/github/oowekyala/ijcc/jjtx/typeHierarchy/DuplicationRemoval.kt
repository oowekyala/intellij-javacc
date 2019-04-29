package com.github.oowekyala.ijcc.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Category.DUPLICATE_MATCH
import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.util.asMap
import com.github.oowekyala.ijcc.util.associateByToMostlySingular


fun TypeHierarchyTree.removeDuplicates(ctx: JjtxRunContext): TypeHierarchyTree {

    val copy = deepCopy()

    val multi = copy.descendants().associateByToMostlySingular { it.nodeName }

    for ((name, dups) in multi.asMap()) {
        if (dups.size > 1) {
            val mostSpecific = dups.maxBy { it.specificity.ordinal }!!

            ctx.errorCollector.handleError(
                "Node $name is matched by several productions, selecting ${mostSpecific.positionInfo} by specificity",
                DUPLICATE_MATCH,
                sourcePosition = mostSpecific.positionInfo
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
