package com.github.oowekyala.jjtx.jjtree

import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner

data class NodeVar(
    val varName: String,
    val enclosingVar: NodeVar?,
    val closedVar: String,
    val exceptionVar: String,
    val owner: JjtNodeClassOwner,
    /** Raw name of the node. */
    val nodeName: String,
    /** QName of the class. */
    val nodeQname: String,
    /** Type of the jjtThis variable. */
    val nodeRefType: String = nodeQname
) {
    val nodeSimpleName = nodeQname.substringAfterLast('.')
}
