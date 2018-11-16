package com.github.oowekyala.gark87.idea.javacc.structure

import com.github.oowekyala.gark87.idea.javacc.psi.JavaccPsiElement
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import java.util.*

/**
 * Base class for leafs of the structure view.
 *
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JavaccLeafElement<T : JavaccPsiElement>(declaration: T) : PsiTreeElementBase<T>(declaration),
    JavaccStructureViewElement {

    override fun getChildrenBase(): Collection<StructureViewTreeElement> = ArrayList()

}
