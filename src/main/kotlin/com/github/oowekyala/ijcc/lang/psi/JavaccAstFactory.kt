package com.github.oowekyala.ijcc.lang.psi

import com.intellij.lang.ASTFactory
import com.intellij.lang.DefaultASTFactory
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.FileElement
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

/**
 * Extension point.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JavaccAstFactory : ASTFactory() {

    private val myDefaultASTFactory = ServiceManager.getService(DefaultASTFactory::class.java)

    override fun createComposite(type: IElementType): CompositeElement? {
        return if (type is IFileElementType) {
            FileElement(type, null)
        } else CompositeElement(type)
    }

    override fun createLeaf(type: IElementType, text: CharSequence): LeafElement? {
        return when {
            JccTypesExt.CommentTypeSet.contains(type) -> myDefaultASTFactory.createComment(type, text)
            else                                      -> LeafPsiElement(type, text)
        }
    }
}
