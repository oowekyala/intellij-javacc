package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.JavaccConfig
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

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

    @JvmDefault
    override fun nodeClass(javaccConfig: JavaccConfig): NavigatablePsiElement? {
        if (this.isVoid) return null

        val nodePackage = javaccConfig.nodePackage
        val nodeName = javaccConfig.nodePrefix + this.name

        return JccNodeClassOwner.getJavaClassFromQname(this, "$nodePackage.$nodeName")
    }

    /**
     * Gets the production header of the production to which this
     * descriptor is applied. If null then [expansionUnit] won't
     * return null, bc this is applied to a single expansion unit.
     */
    @JvmDefault
    val productionHeader: JccJavaNonTerminalProductionHeader?
        get() = prevSiblingNonWhitespace() as? JccJavaNonTerminalProductionHeader

    /**
     * Gets the expansion unit that is the scope of this node.
     * If null then [productionHeader] won't return null, bc this
     * is applied to a whole production.
     */
    @JvmDefault
    val expansionUnit: JccExpansionUnit?
        get() = prevSiblingNonWhitespace() as? JccExpansionUnit

    private fun prevSiblingNonWhitespace(): PsiElement =
            if (prevSibling.node.elementType == TokenType.WHITE_SPACE)
                prevSibling.prevSibling
            else prevSibling

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