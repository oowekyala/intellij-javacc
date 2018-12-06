package com.github.oowekyala.ijcc.insight.structure

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
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
class JavaccFileStructureViewModel(psiFile: JccFileImpl)
    : StructureViewModelBase(psiFile, JccStructureTreeElement(psiFile)), StructureViewModel.ElementInfoProvider {

    init {
        withSuitableClasses(
            JccFileImpl::class.java,
            JccOptionSection::class.java,
            JccOptionBinding::class.java,

            // FUTURE JccParserDeclaration::class.java,

            JccRegularExprProduction::class.java,
            JccRegexprSpec::class.java,

            JccNonTerminalProduction::class.java
        )
    }


    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegularExprProduction || value is JccOptionSection
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        val value = element.value
        return value is JccRegexprSpec || value is JccNonTerminalProduction
    }


    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)
    override fun getFilters(): Array<Filter> = arrayOf(terminalFilter(), optionFilter())

    companion object {

        // optionally sort alphabetically like Kotlin structure does


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