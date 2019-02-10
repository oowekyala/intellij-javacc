package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.firstAncestorOrNull
import com.github.oowekyala.ijcc.util.associateByToMostlySingular
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.pom.Navigatable

/**
 * Root of the structure view tree.
 *
 * @author Clément Fournier
 * @since 1.1
 */
class JccFileStructureTreeElement(private val myFile: JccFile)
    : StructureViewTreeElement, Navigatable by myFile {

    override fun getPresentation(): ItemPresentation = myFile.getPresentationForStructure()

    override fun getValue(): JccFile = myFile

    private val myChildren: Array<JccStructureTreeElement>

    override fun getChildren(): Array<out TreeElement> = myChildren


    init {

        // only iterates the tokens of the lexical grammar once instead of once per production.

        with(myFile) {
            val syntheticTokensByProd =
                lexicalGrammar
                    .defaultState
                    .tokens
                    .asSequence()
                    .mapNotNull { it as? SyntheticToken }
                    .associateByToMostlySingular({ it.regularExpression!!.firstAncestorOrNull<JccNonTerminalProduction>()!! }) {
                        it.declUnit!!
                    }

            val nonTerminalChildren =
                nonTerminalProductions.map {
                    JccStructureTreeElement(
                        it,
                        syntheticTokensByProd[it].map(::JccStructureTreeElement)
                    )
                }

            val regexChildren =
                regexProductions.map { JccStructureTreeElement(it, it.regexSpecList.map(::JccStructureTreeElement)) }


            val optionsNode =
                options?.let { JccStructureTreeElement(it, it.optionBindingList.map(::JccStructureTreeElement)) }
                    ?.let { sequenceOf(it) }
                    .orEmpty()

            val otherLeaves =
                listOfNotNull(parserDeclaration, tokenManagerDecls.firstOrNull()).map(::JccStructureTreeElement)

            myChildren = sequenceOf(
                optionsNode,
                otherLeaves.asSequence(),
                regexChildren,
                nonTerminalChildren
            )
                .flatMap { it }
                .sortedBy { it.element.textOffset }
                .toList()
                .toTypedArray()
        }


    }


}