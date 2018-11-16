package com.github.oowekyala.gark87.idea.javacc.structureview

import com.github.oowekyala.idea.javacc.psi.JavaccFileImpl
import com.github.oowekyala.idea.javacc.util.JavaCCIcons
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
class JavaccFileStructureViewModel(psiFile: JavaccFileImpl) : StructureViewModelBase(psiFile, JavaccFileTreeElement(psiFile)) {

    override fun getFilters(): Array<Filter> = arrayOf(terminalFilter())

    companion object {
        private fun terminalFilter(): Filter = object : FileStructureFilter {
            override fun getCheckBoxText(): String = "Show terminals"

            override fun getShortcut(): Array<Shortcut> = emptyArray()

            override fun isVisible(treeElement: TreeElement): Boolean = true // TODO

            override fun isReverted(): Boolean = true

            override fun getPresentation(): ActionPresentation = ActionPresentationData("text", "description", JavaCCIcons.TERMINAL.icon)

            override fun getName(): String = "Terminals"
        }
    }
}
