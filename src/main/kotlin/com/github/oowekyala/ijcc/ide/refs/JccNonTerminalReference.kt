package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.ide.completion.withTail
import com.github.oowekyala.ijcc.ide.structureview.getPresentableText
import com.github.oowekyala.ijcc.ide.structureview.getPresentationIcon
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.intellij.codeInsight.completion.simple.ParenthesesTailType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.codeStyle.CommonCodeStyleSettings


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


    override fun getVariants(): Array<Any?> =
        element.containingFile.nonTerminalProductions.map {
            LookupElementBuilder.create(it.name)
                .withPsiElement(it)
                .withPresentableText(it.getPresentableText())
                .withIcon(it.getPresentationIcon())
                .withTail("() ")
        }
            .toList()
            .plus(LookaheadLookupItem)
            .toTypedArray()

    override fun calculateDefaultRangeInElement(): TextRange = element.nameIdentifier.textRangeInParent

    override fun handleElementRename(newElementName: String?): PsiElement = newElementName.toString().let {
        val id = element.nameIdentifier
        JccIdentifierManipulator().handleContentChange(id, newElementName)!!
    }

    companion object {

        // TODO move to completion contributor with a proper pattern
        private val LookaheadLookupItem =
            LookupElementBuilder.create("LOOKAHEAD")
                .withBoldness(true)
                .withPresentableText("LOOKAHEAD")
                .withTailText("(...)", true)
                .withTail(object : ParenthesesTailType() {
                    override fun isSpaceWithinParentheses(styleSettings: CommonCodeStyleSettings?,
                                                          editor: Editor?,
                                                          tailOffset: Int): Boolean = false

                    override fun isSpaceBeforeParentheses(styleSettings: CommonCodeStyleSettings?,
                                                          editor: Editor?,
                                                          tailOffset: Int): Boolean = false
                })
    }

}