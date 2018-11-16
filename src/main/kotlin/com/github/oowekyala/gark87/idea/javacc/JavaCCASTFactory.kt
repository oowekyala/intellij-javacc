package com.github.oowekyala.gark87.idea.javacc

import com.github.oowekyala.gark87.idea.javacc.generated.JavaCCConstants
import com.github.oowekyala.gark87.idea.javacc.psi.Identifier
import com.intellij.lang.ASTFactory
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.FileElement
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

/**
 * @author gark87
 */
class JavaCCASTFactory : ASTFactory() {
    override fun createComposite(type: IElementType): CompositeElement? {
        return if (type is IFileElementType) {
            FileElement(type, null)
        } else CompositeElement(type)
    }

    override fun createLeaf(type: IElementType, text: CharSequence): LeafElement? {
        return if (type === JavaCCConstants.IDENTIFIER || type === JavaCCConstants._OPTIONS) {
            Identifier(type, text)
        } else LeafPsiElement(type, text)
    }
}
