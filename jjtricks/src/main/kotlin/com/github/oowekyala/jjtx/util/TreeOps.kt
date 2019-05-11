package com.github.oowekyala.jjtx.util

import com.github.oowekyala.treeutils.TreeLikeExtensions

/**
 * @author Clément Fournier
 */
interface TreeOps<Self : TreeOps<Self>> : TreeLikeExtensions<Self> {


    fun children(): Sequence<Self> = myself().children.asSequence()
    fun descendants(): Sequence<Self> = myself().descendants()
    fun descendantsOrSelf(): Sequence<Self> = myself().descendantsOrSelf()

    private fun myself(): Self = this as Self

}


