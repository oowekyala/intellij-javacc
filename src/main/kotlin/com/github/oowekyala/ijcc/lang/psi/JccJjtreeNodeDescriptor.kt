package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JccTypes
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
interface JccJjtreeNodeDescriptor : JccPsiElement, JccIdentifierOwner {

    /**
     * Returns the expression if one was specified.
     */
        val descriptorExpr: JccJjtreeNodeDescriptorExpr?
        get() = lastChildNoWhitespace as JccJjtreeNodeDescriptorExpr?

    /** Is null if this is void. */
    override fun getNameIdentifier(): JccIdentifier?

    /** Either the identifier or the void element. */
        val namingLeaf: PsiElement
        get() = nameIdentifier ?: node.findChildByType(JccTypes.JCC_VOID_KEYWORD)!!.psi

    /**
     * Gets the production header of the production to which this
     * descriptor is applied. If null then [expansionUnit] won't
     * return null, bc this is applied to a single expansion unit.
     */
        val productionHeader: JccJavaNonTerminalProductionHeader?
        get() = parent.let { it as? JccNonTerminalProduction }?.header

    /**
     * Gets the expansion unit that is the scope of this node.
     * If null then [productionHeader] won't return null, bc this
     * is applied to a whole production.
     */
        val expansionUnit: JccExpansionUnit?
        get() = parent.let { it as? JccScopedExpansionUnit }?.expansionUnit

    /**
     * Returns true if this is a "#void" annotation.
     */
        val isVoid: Boolean
        get() = nameIdentifier == null

        val isIndefinite: Boolean
        get() = descriptorExpr == null

        val isGreaterThan: Boolean
        get() = descriptorExpr != null && descriptorExpr!!.isGtExpression
}
