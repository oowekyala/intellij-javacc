package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.FileStructureFilter
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.openapi.actionSystem.Shortcut

/**
 * Model for the structure view, implements logic for filter buttons and sorting.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFileStructureViewModel(psiFile: JccFile)
    : StructureViewModelBase(psiFile, JccStructureTreeElement(psiFile)),
    StructureViewModel.ElementInfoProvider {

    init {
        withSuitableClasses(
            JccFile::class.java,
            JccOptionSection::class.java,
            JccOptionBinding::class.java,

            JccParserDeclaration::class.java,
            JccTokenManagerDecls::class.java,

            JccRegularExprProduction::class.java,
            JccRegexprSpec::class.java,
            JccRegexpExpansionUnit::class.java,

            JccNonTerminalProduction::class.java
        )
    }


    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegularExprProduction || value is JccOptionSection || value is JccNonTerminalProduction
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegexprSpec || value is JccRegexpExpansionUnit
                // TODO these should not be leaves, ideally their declarations would be shown as well
                || value is JccTokenManagerDecls || value is JccParserDeclaration
    }


    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)
    override fun getFilters(): Array<Filter> = arrayOf(terminalFilter(), optionFilter())

    companion object {

        private fun optionFilter(): Filter = object : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show Options"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean =
                    when (treeElement.let { it as JccStructureTreeElement }.element) {
                        is JccOptionSection -> false
                        is JccOptionBinding -> false
                        else                -> true
                    }

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation =
                    ActionPresentationData(
                        "Show JavaCC Options",
                        "Show the options for code generation",
                        JavaccIcons.JAVACC_OPTION
                    )

            override fun getName(): String = "OptionFilter"
        }

        private fun terminalFilter(): Filter = object : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show Lexical Structure"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean =
                    when (treeElement.let { it as JccStructureTreeElement }.element) {
                        is JccRegexprSpec           -> false
                        is JccRegularExprProduction -> false
                        is JccRegexpExpansionUnit   -> false
                        else                        -> true
                    }

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation =
                    ActionPresentationData(
                        "Show Lexical Structure",
                        "Show tokens specifications.",
                        JavaccIcons.TOKEN_HEADER
                    )

            override fun getName(): String = "TerminalFilter"
        }
    }

}
