package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.model.LexicalState
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.icons.JavaccIcons
import com.intellij.codeInsight.lookup.LookupElementBuilder
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
        element.containingFile
            .lexicalGrammar
            .lexicalStates
            .asSequence()
            .mapNotNull {
                LookupElementBuilder.create(it.name)
                    .withPsiElement(it.declarationIdent)
                    .withIcon(JavaccIcons.LEXICAL_STATE)
            }
            .toList()
            .toTypedArray()
}