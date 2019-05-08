package com.github.oowekyala.jjtx.typeHierarchy

import com.github.oowekyala.jjtx.reporting.MessageCategory.DUPLICATE_MATCH
import com.github.oowekyala.ijcc.util.asMap
import com.github.oowekyala.ijcc.util.associateByToMostlySingular
import com.github.oowekyala.jjtx.JjtxContext


internal fun TypeHierarchyTree.removeDuplicates(ctx: JjtxContext): TypeHierarchyTree {

    val copy = deepCopy()

    val multi = copy.descendants().associateByToMostlySingular { it.nodeName }

    for ((name, dups) in multi.asMap()) {
        if (dups.size > 1) {
            val mostSpecific = dups.maxBy { it.specificity.ordinal }!!

            ctx.messageCollector.report(
                "Node $name is matched by several productions, most specific will be chosen",
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
