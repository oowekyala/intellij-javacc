package com.github.oowekyala.ijcc.ide.structureview

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.*
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
class JavaccFileStructureViewModel(val psiFile: JccFile)
    : StructureViewModelBase(psiFile, JccFileStructureTreeElement(psiFile)),
    StructureViewModel.ElementInfoProvider {

    init {
        withSuitableClasses(
            JccFile::class.java,
            JccOptionSection::class.java,
            JccOptionBinding::class.java,

            JccParserDeclaration::class.java,
            JccTokenManagerDecls::class.java,
            JccScopedExpansionUnit::class.java,

            JccRegexProduction::class.java,
            JccRegexSpec::class.java,
            JccRegexExpansionUnit::class.java,

            JccNonTerminalProduction::class.java
        )
    }


    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegexProduction || value is JccOptionSection || value is JccNonTerminalProduction
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegexSpec || value is JccRegexExpansionUnit
            // TODO these should not be leaves, ideally their declarations would be shown as well
            || value is JccTokenManagerDecls || value is JccParserDeclaration
    }


    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)
    override fun getFilters(): Array<Filter> = arrayOf(TerminalFilter, OptionFilter)

    companion object {

        object OptionFilter : FileStructureFilter {
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
                    JccIcons.JAVACC_OPTION
                )

            override fun getName(): String = "OptionFilter"
        }

        object TerminalFilter : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show Lexical Structure"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean =
                when (treeElement.let { it as JccStructureTreeElement }.element) {
                    is JccRegexSpec          -> false
                    is JccRegexProduction    -> false
                    is JccRegexExpansionUnit -> false
                    else                     -> true
                }

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation =
                ActionPresentationData(
                    "Show Lexical Structure",
                    "Show tokens specifications.",
                    JccIcons.TOKEN_HEADER
                )

            override fun getName(): String = "TerminalFilter"
        }
    }

}
