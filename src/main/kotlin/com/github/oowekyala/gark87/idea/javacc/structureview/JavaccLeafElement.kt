package com.github.oowekyala.gark87.idea.javacc.structureview

import com.github.oowekyala.idea.javacc.psi.DeclarationForStructureView
import com.github.oowekyala.idea.javacc.psi.Identifier
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import java.util.*

/**
 * Base class for leafs of the structure view.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JavaccLeafElement(declaration: DeclarationForStructureView) : PsiTreeElementBase<Identifier>(declaration.identifier) {

    override fun getChildrenBase(): Collection<StructureViewTreeElement> = ArrayList()

    override fun getPresentableText(): String? {
        val element = element ?: return ""
        return element.name
    }
}
