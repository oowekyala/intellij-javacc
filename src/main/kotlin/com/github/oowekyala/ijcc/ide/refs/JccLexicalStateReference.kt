package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * @author Clément Fournier
 * @since 1.2
 */
class JccLexicalStateReference(element: JccIdentifier) : PsiReferenceBase<JccIdentifier>(element) {

    override fun resolve(): PsiElement? = resolveState()?.declarationIdent

    fun resolveState(): LexicalState? =
        element.containingFile
            .lexicalGrammar
            .getLexicalState(element.name)


    override fun getVariants(): Array<Any> =
        JccRefVariantService.getInstance().lexicalStateVariants(this)
}
