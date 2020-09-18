package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
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

        return element.containingFile.getProductionByName(searchedName)
    }

    override fun isReferenceTo(elt: PsiElement): Boolean {
        return when (elt) {
            is JccNonTerminalProduction -> elt.name == element.name
            is JccIdentifier            -> elt.owner?.let { isReferenceTo(it) } == true
            else                        -> false
        }
    }


    override fun getVariants(): Array<Any> =
        JccRefVariantService.getInstance().nonterminalRefVariants(this)

    override fun calculateDefaultRangeInElement(): TextRange = element.nameIdentifier.textRangeInParent

    override fun handleElementRename(newElementName: String): PsiElement = newElementName.let {
        val id = element.nameIdentifier
        JccIdentifierManipulator().handleContentChange(id, newElementName)!!
    }

}
