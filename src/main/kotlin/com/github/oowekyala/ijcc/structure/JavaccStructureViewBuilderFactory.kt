package com.github.oowekyala.ijcc.structure

import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
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
        return when (psiFile) {
            !is JccFileImpl -> null
            else            -> object : TreeBasedStructureViewBuilder() {

                override fun isRootNodeShown(): Boolean = false

                override fun createStructureViewModel(editor: Editor?): StructureViewModel =
                        JavaccFileStructureViewModel(psiFile)
            }
        }
    }
}
