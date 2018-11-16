package com.github.oowekyala.gark87.idea.javacc.structure

import com.github.oowekyala.gark87.idea.javacc.psi.JavaccFileImpl
import com.github.oowekyala.gark87.idea.javacc.util.JavaCCIcons
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.util.FileStructureFilter
import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.openapi.actionSystem.Shortcut

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccFileStructureViewModel(psiFile: JavaccFileImpl) :
    StructureViewModelBase(psiFile, JavaccFileTreeElement(psiFile)) {

    override fun getFilters(): Array<Filter> = arrayOf(terminalFilter(), nonTerminalFilter())

    companion object {
        private fun terminalFilter(): Filter = object : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show terminals"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean = treeElement !is TerminalStructureLeaf

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation =
                ActionPresentationData("Show terminals", "Show terminals (tokens)", JavaCCIcons.TERMINAL.icon)

            override fun getName(): String = "TerminalFilter"
        }


        private fun nonTerminalFilter(): Filter = object : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show non-terminals"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean = treeElement !is NonTerminalStructureNode

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation =
                ActionPresentationData(
                    "Show non-terminals",
                    "Show non-terminals (productions)",
                    JavaCCIcons.NONTERMINAL.icon
                )

            override fun getName(): String = "NonTerminalFilter"
        }
    }
}
