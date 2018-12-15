package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes

/**
 * Expression in a [JccJjtreeNodeDescriptor].
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccJjtreeNodeDescriptorExpr : JavaccPsiElement {

    @JvmDefault
    val isGtExpression: Boolean
        // the first child is the parenthesis
        get() = firstChild.nextSiblingNoWhitespace?.node?.elementType === JavaccTypes.JCC_GT

    val javaExpression: JccJavaExpression
}