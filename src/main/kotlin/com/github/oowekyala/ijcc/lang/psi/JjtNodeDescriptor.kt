package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes

/**
 * Node descriptor.
 *
 *
 * JjtNodeDescriptor ::= "#" ( [JccIdentifier] | "void" ) [ "(" [JjtNodeDescriptorExpr] ")" ]
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JjtNodeDescriptor : JavaccPsiElement {


    /**
     * Returns the expression if one was specified
     */
    val descriptorExpr: JjtNodeDescriptorExpr?
        get() = when (lastChild) {
            is JjtNodeDescriptorExpr -> lastChild as JjtNodeDescriptorExpr
            else -> null
        }

    /**
     * Returns true if this is a void identifier.
     */
    val isVoid: Boolean
        get() = children[1].node.elementType === JavaccTypes.JCC_VOID_KEYWORD

    /**
     * Returns the identifier if this isn't void.
     */
    val nameIdentifier: JccIdentifier?
        get() = if (isVoid) null else children[1] as JccIdentifier


    val isIndefinite: Boolean
        get() = descriptorExpr == null

    val isGreaterThan: Boolean
        get() = descriptorExpr != null && descriptorExpr!!.isGtExpression
}