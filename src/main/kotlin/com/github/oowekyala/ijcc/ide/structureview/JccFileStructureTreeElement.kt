package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.model.GrammarNature.JJTREE
import com.github.oowekyala.ijcc.lang.model.SyntheticToken
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.associateByToMostlySingular
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.navigation.ItemPresentation
import com.intellij.util.containers.MostlySingularMultiMap
import javax.swing.Icon

/**
 * Root of the structure view tree.
 *
 * @author Clément Fournier
 * @since 1.1
 */
class JccFileStructureTreeElement(private val myFile: JccFile) : PsiTreeElementBase<JccFile>(myFile) {

    override fun getPresentation(): ItemPresentation = myFile.presentationForStructure

    override fun getPresentableText(): String? = myFile.presentableText

    override fun getIcon(open: Boolean): Icon? = myFile.presentationIcon

    override fun getValue(): JccFile = myFile

    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        // only iterates the tokens of the lexical grammar once instead of once per production.

        with(myFile) {
            val syntheticTokensByProd: MostlySingularMultiMap<JccNonTerminalProduction, JccRegexExpansionUnit?> =
                lexicalGrammar
                    .defaultState
                    .tokens
                    .asSequence()
                    .mapNotNull { it as? SyntheticToken }
                    .associateByToMostlySingular({ it.regularExpression?.firstAncestorOrNull<JccNonTerminalProduction>() }) {
                        it.declUnit
                    }


            val nonTerminalChildren =
                nonTerminalProductions.map {

                    val jjtNodes =
                        when {
                            it is JccBnfProduction && myFile.grammarNature >= JJTREE ->
                                it.expansion
                                    ?.descendantSequence(includeSelf = true)
                                    ?.filterIsInstance<JccScopedExpansionUnit>()
                                    .orEmpty()
                            else                                                     -> emptySequence()
                        }

                    JccStructureTreeElement(
                        it,
                        jjtNodes
                            .plus(syntheticTokensByProd[it].filterNotNull())
                            .sortedBy(JccPsiElement::getTextOffset)
                            .map(::JccStructureTreeElement)
                            .toList()

                    )
                }

            val regexChildren =
                regexProductions.map { JccStructureTreeElement(it, it.regexSpecList.map(::JccStructureTreeElement)) }


            val optionsNode =
                options?.let { JccStructureTreeElement(it, it.optionBindingList.map(::JccStructureTreeElement)) }
                    ?.let { sequenceOf(it) }
                    .orEmpty()

            val otherLeaves =
                listOfNotNull(tokenManagerDecls.firstOrNull()).map(::JccStructureTreeElement)

            val parserClass = myFile.classes.toList().map { JccJavaClassTreeElementWrapper(it, myFile) }

            return sequenceOf(
                optionsNode,
                otherLeaves.asSequence(),
                parserClass.asSequence(),
                regexChildren,
                nonTerminalChildren
            )
                .flatMap { it }
                .toList()
        }
    }
}
