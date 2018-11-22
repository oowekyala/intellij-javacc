package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes

/**
 * Node descriptor.
 *
 *
 * JjtNodeDescriptor ::= "#" ( [JccIdentifier] | "void" ) [ "(" [JccJjtreeNodeDescriptorExpr] ")" ]
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccJjtreeNodeDescriptor : JavaccPsiElement, JccIdentifierOwner {

    /**
     * Returns the expression if one was specified
     */
    @JvmDefault
    val descriptorExpr: JccJjtreeNodeDescriptorExpr?
        get() = when (lastChild) {
            is JccJjtreeNodeDescriptorExpr -> lastChild as JccJjtreeNodeDescriptorExpr
            else                           -> null
        }

    /**
     * Returns true if this is a void identifier.
     */
    @JvmDefault
    val isVoid: Boolean
        get() = children[1].node.elementType === JavaccTypes.JCC_VOID_KEYWORD

    @JvmDefault
    val isIndefinite: Boolean
        get() = descriptorExpr == null

    @JvmDefault
    val isGreaterThan: Boolean
        get() = descriptorExpr != null && descriptorExpr!!.isGtExpression
}