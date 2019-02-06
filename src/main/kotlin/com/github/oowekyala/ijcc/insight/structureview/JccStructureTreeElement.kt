package com.github.oowekyala.ijcc.insight.structureview

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
class JccStructureTreeElement(val element: JccPsiElement)
    : StructureViewTreeElement, SortableTreeElement, Navigatable by element {

    override fun getValue(): Any = element

    override fun getAlphaSortKey(): String = when (element) {
        is JccOptionSection         -> "aaaaaaa"
        is JccParserDeclaration     -> "aaaaaaZ"
        is JccTokenManagerDecls     -> "aaaaaZZ"
        is JccRegularExprProduction -> "aaaaZZZ"
        else                        -> element.getPresentableText()
    }

    override fun getChildren(): Array<TreeElement> = when (element) {
        is JccFile                  ->
            listOfNotNull(
                element.options,
                element.parserDeclaration,
                element.tokenManagerDecls.firstOrNull()
            )
                .plus(element.regexpProductions)
                .plus(element.nonTerminalProductions)

        is JccRegularExprProduction -> element.regexprSpecList
        is JccOptionSection         -> element.optionBindingList
        is JccBnfProduction         ->
            element.expansion
                ?.descendantSequence(includeSelf = true)
                ?.filterIsInstance<JccRegexpExpansionUnit>()
                // consider only synthetic tokens
                ?.filter { it.referencedToken?.isExplicit == false }
                ?.toList().orEmpty()
        else                        -> emptyList()
    }
        .map(::JccStructureTreeElement)
        .toTypedArray()


    override fun getPresentation(): ItemPresentation = element.getPresentationForStructure()

}
