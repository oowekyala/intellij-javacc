package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.insight.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.lang.refs.JccStringTokenReference
import com.github.oowekyala.ijcc.lang.refs.JccTerminalReference
import com.github.oowekyala.ijcc.lang.refs.JjtNodePolyReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

/**
 * Base impl for Jcc psi elements.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JavaccPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JavaccPsiElement {

    override val grammarOptions: GrammarOptions
        get() = containingFile.grammarOptions

    override fun getContainingFile() = super.getContainingFile() as JccFile

    override fun getName(): String? = when (this) {
        is JccIdentifierOwner -> this.nameIdentifier?.name
        else                  -> null
    }

    override fun getReference(): PsiReference? = when (this) {
        is JccTokenReferenceUnit       -> JccTerminalReference(this)
        is JccNonTerminalExpansionUnit -> JccNonTerminalReference(this)
        is JccLiteralRegexpUnit        -> JccStringTokenReference(this)
        is JccNodeClassOwner           -> nodeIdentifier?.let { JjtNodePolyReference(this, it) }
        else                           -> null
    }
}