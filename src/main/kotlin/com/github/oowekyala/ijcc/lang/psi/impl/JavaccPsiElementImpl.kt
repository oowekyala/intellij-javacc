package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.reference.JccNonTerminalReference
import com.github.oowekyala.ijcc.reference.JccStringTokenReference
import com.github.oowekyala.ijcc.reference.JccTerminalReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.strictParents

/**
 * Base impl for Jcc psi elements.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JavaccPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JavaccPsiElement {

    override fun getContainingFile() = super.getContainingFile() as JccFile

    override fun getName(): String? = when (this) {
        is JccIdentifierOwner -> this.nameIdentifier?.name
        // JccIdentifier overrides this directly
        else                  -> null
    }

    override fun getReference(): PsiReference? = when (this) {
        is JccLiteralRegularExpression   -> JccStringTokenReference(this, this.isInRegexContext())
        is JccNonTerminalExpansionUnit   -> JccNonTerminalReference(this.nameIdentifier)
        is JccRegularExpressionReference -> JccTerminalReference(this.nameIdentifier, this.isInRegexContext())
        else                             -> null
    }

    private fun JccRegularExpression.isInRegexContext(): Boolean =
            strictParents().any { it is JccRegularExprProduction }

}