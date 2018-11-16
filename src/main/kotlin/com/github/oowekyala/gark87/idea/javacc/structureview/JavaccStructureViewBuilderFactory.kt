package com.github.oowekyala.gark87.idea.javacc.structureview

import com.github.oowekyala.gark87.idea.javacc.psi.JavaccFileImpl
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * @author gark87
 */
class JavaccStructureViewBuilderFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        return if (psiFile !is JavaccFileImpl) {
            null
        } else object : TreeBasedStructureViewBuilder() {

            override fun isRootNodeShown(): Boolean = false

            override fun createStructureViewModel(editor: Editor?): StructureViewModel = JavaccFileStructureViewModel(psiFile)
        }
    }
}
