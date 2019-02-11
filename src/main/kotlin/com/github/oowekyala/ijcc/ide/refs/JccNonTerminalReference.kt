package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.ide.completion.withTail
import com.github.oowekyala.ijcc.ide.structureview.getPresentableText
import com.github.oowekyala.ijcc.ide.structureview.getPresentationIcon
import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.github.oowekyala.ijcc.lang.psi.textRangeInParent
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase


/**
 * Reference to a [JccNonTerminalProduction].
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccNonTerminalReference(psiElement: JccNonTerminalExpansionUnit) :
    PsiReferenceBase<JccNonTerminalExpansionUnit>(psiElement) {

    override fun resolve(): JccIdentifier? = resolveProduction()?.nameIdentifier

    fun resolveProduction(): JccNonTerminalProduction? {
        val searchedName = element.name ?: return null

        return element.containingFile.syntaxGrammar.getProductionByName(searchedName)
    }


    override fun getVariants(): Array<Any?> =
        element.containingFile.nonTerminalProductions.map {
            LookupElementBuilder.create(it.name)
                .withPsiElement(it)
                .withPresentableText(it.getPresentableText())
                .withIcon(it.getPresentationIcon())
                .withTail("() ")
        }.toList().toTypedArray()

    override fun calculateDefaultRangeInElement(): TextRange = element.nameIdentifier.textRangeInParent

    override fun handleElementRename(newElementName: String?): PsiElement = newElementName.toString().let {
        val id = element.nameIdentifier
        JccIdentifierManipulator().handleContentChange(id, newElementName)!!
    }

}