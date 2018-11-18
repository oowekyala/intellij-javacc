package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes

/**
 * Expression in a [JjtNodeDescriptor].
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JjtNodeDescriptorExpr : JavaccPsiElement {

    val isGtExpression: Boolean
        get() = firstChild.node.elementType === JavaccTypes.JCC_GT

}