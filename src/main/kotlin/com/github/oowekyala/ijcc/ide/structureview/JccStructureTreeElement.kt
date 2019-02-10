package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.pom.Navigatable

/**
 * One element of the structure view. This class is used for all elements, regardless of their type.
 *
 * TODO represent synthetic members as non navigatable?
 *
 */
class JccStructureTreeElement(val element: JccPsiElement,
                              private val lexicalGrammar: LexicalGrammar)
    : StructureViewTreeElement, SortableTreeElement, Navigatable by element {

    override fun getValue(): Any = element

    override fun getAlphaSortKey(): String = when (element) {
        is JccOptionSection     -> "aaaaaaa"
        is JccParserDeclaration -> "aaaaaaZ"
        is JccTokenManagerDecls -> "aaaaaZZ"
        is JccRegexProduction   -> "aaaaZZZ"
        else                    -> element.getPresentableText()
    }

    override fun getChildren(): Array<TreeElement> = when (element) {
        is JccFile            ->
            listOfNotNull(
                element.options,
                element.parserDeclaration,
                element.tokenManagerDecls.firstOrNull()
            )
                .plus(element.regexProductions)
                .plus(element.nonTerminalProductions)

        is JccRegexProduction -> element.regexSpecList
        is JccOptionSection   -> element.optionBindingList
        is JccBnfProduction   -> {
            lexicalGrammar
                .defaultState
                .tokens
                .mapNotNull { it as? SyntheticToken }
                .filter { it.regularExpression?.ancestors(includeSelf = false)?.any { it == element } == true }
                .mapNotNull { it.declUnit }
                .toList()
        }
        else                  -> emptyList()
    }
        .map { JccStructureTreeElement(it, lexicalGrammar) }
        .toTypedArray()

    override fun getPresentation(): ItemPresentation = element.getPresentationForStructure()

}
