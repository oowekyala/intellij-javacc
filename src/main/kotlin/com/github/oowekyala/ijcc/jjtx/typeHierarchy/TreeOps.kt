package com.github.oowekyala.ijcc.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.util.prepend
import com.github.oowekyala.treeutils.TreeLikeAdapter

/**
 * @author Clément Fournier
 */
interface TreeOps<Self : TreeOps<Self>> {


    val adapter: TreeLikeAdapter<Self>


    fun children(): Sequence<Self> = adapter.getChildren(this.myself()).asSequence()


    fun descendants(): Sequence<Self> = children().flatMap { it.tree() }


    private fun myself(): Self = this as Self


    private fun tree(): Sequence<Self> = children().prepend(myself())
}


