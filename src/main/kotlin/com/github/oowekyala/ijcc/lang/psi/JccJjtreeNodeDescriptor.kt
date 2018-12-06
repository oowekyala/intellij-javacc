package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.intellij.psi.PsiElement

/**
 * Node descriptor.
 *
 *
 * JjtNodeDescriptor ::= "#" ( [JccIdentifier] | "void" ) [ "(" [JccJjtreeNodeDescriptorExpr] ")" ]
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccJjtreeNodeDescriptor : JavaccPsiElement, JccIdentifierOwner, JccNodeClassOwner {

    /**
     * Returns the expression if one was specified
     */
    @JvmDefault
    val descriptorExpr: JccJjtreeNodeDescriptorExpr?
        get() = when (lastChild) {
            is JccJjtreeNodeDescriptorExpr -> lastChild as JccJjtreeNodeDescriptorExpr
            else                           -> null
        }

    /** Is null if this is void. */
    override fun getNameIdentifier(): JccIdentifier?

    /** Either the identifier or the void element. */
    @JvmDefault
    val namingLeaf: PsiElement
        get() = nameIdentifier ?: node.findChildByType(JavaccTypes.JCC_VOID_KEYWORD)!!.psi

    /**
     * Gets the production header of the production to which this
     * descriptor is applied. If null then [expansionUnit] won't
     * return null, bc this is applied to a single expansion unit.
     */
    @JvmDefault
    val productionHeader: JccJavaNonTerminalProductionHeader?
        get() = parent.let { it as? JccNonTerminalProduction }?.header

    /**
     * Gets the expansion unit that is the scope of this node.
     * If null then [productionHeader] won't return null, bc this
     * is applied to a whole production.
     */
    @JvmDefault
    val expansionUnit: JccExpansionUnit?
        get() = parent.let { it as? JccScopedExpansionUnit }?.expansionUnit

    /**
     * Returns true if this is a "#void" annotation.
     */
    @JvmDefault
    val isVoid: Boolean
        get() = nameIdentifier == null

    @JvmDefault
    val isIndefinite: Boolean
        get() = descriptorExpr == null

    @JvmDefault
    val isGreaterThan: Boolean
        get() = descriptorExpr != null && descriptorExpr!!.isGtExpression
}