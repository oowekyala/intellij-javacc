package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.ide.refs.JccBnfStringLiteralReference
import com.github.oowekyala.ijcc.ide.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.ide.refs.JccTerminalReference
import com.github.oowekyala.ijcc.lang.model.GrammarOptions
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope

/**
 * Base impl for Jcc psi elements.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JccPsiElement {

    override val grammarOptions: GrammarOptions
        get() = containingFile.grammarOptions

    override fun getContainingFile() = super.getContainingFile() as JccFile

    override fun getName(): String? = when (this) {
        is JccIdentifierOwner -> this.nameIdentifier?.name
        else                  -> null
    }

    override fun getTextOffset(): Int = when {
        this is PsiNameIdentifierOwner && this.nameIdentifier != null -> nameIdentifier!!.textOffset
        else                                                          -> super.getTextOffset()
    }

    // TODO should also consider Jjtree class files
    override fun getResolveScope() = GlobalSearchScope.fileScope(containingFile)

    override fun getUseScope(): SearchScope = GlobalSearchScope.fileScope(containingFile)

    override fun getReference(): PsiReference? = when (this) {
        is JccTokenReferenceRegexUnit  -> JccTerminalReference(this)
        is JccNonTerminalExpansionUnit -> JccNonTerminalReference(this)
        is JccLiteralRegexUnit         -> if (isStringToken) JccBnfStringLiteralReference(this) else null
        // Having it here breaks the Find Usages function
        // see ReferenceExtensions.typedReference
        // is JccNodeClassOwner        -> JjtNodePolyReference(this).takeIf { isNotVoid }
        else                           -> null
    }
}