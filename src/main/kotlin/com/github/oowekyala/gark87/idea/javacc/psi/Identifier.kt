package com.github.oowekyala.gark87.idea.javacc.psi

import com.github.oowekyala.idea.javacc.psi.reference.IdentifierReference
import com.github.oowekyala.idea.javacc.psi.reference.JavaCCScopeProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.NonNls

/**
 * @author gark87
 */
class Identifier(type: IElementType, text: CharSequence) : LeafPsiElement(type, text), PsiNameIdentifierOwner {

    override fun getNameIdentifier(): PsiElement? = this

    override fun getReference(): PsiReference? {
        val parent = parent ?: return null
        if (parent.node.elementType !== JavaCCTreeConstants.JJTIDENTIFIER) {
            val elementType = parent.node.elementType
            if (isNonTerminalProduction(elementType)) {
                return IdentifierReference(this, JavaCCScopeProcessor.NONTERMINAL_OR_VAR)
            }
        }
        val grandParent = parent.parent ?: return null
        val elementType = grandParent.node.elementType
        if (isNonTerminalProduction(elementType)) {
            return IdentifierReference(this, JavaCCScopeProcessor.NONTERMINAL)
        }
        return if (elementType === JavaCCTreeConstants.JJTREGULAR_EXPRESSION) {
            IdentifierReference(this, JavaCCScopeProcessor.TOKEN)
        } else IdentifierReference(this, JavaCCScopeProcessor.VARIABLE)
    }

    override fun getName(): String = text

    @Throws(IncorrectOperationException::class)
    override fun setName(@NonNls s: String): PsiElement = this

    private fun isNonTerminalProduction(elementType: IElementType): Boolean {
        return elementType === JavaCCTreeConstants.JJTBNF_PRODUCTION ||
               elementType === JavaCCTreeConstants.JJTJAVACODE_PRODUCTION ||
               elementType === JavaCCTreeConstants.JJTEXPANSION_UNIT
    }
}
