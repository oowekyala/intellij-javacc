package com.github.oowekyala.ijcc.lang.psi

import com.intellij.lang.ASTFactory
import com.intellij.lang.DefaultASTFactory
import com.intellij.openapi.application.ApplicationManager
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
class JavaccAstFactory : ASTFactory() {

    override fun createComposite(type: IElementType): CompositeElement {
        return if (type is IFileElementType) {
            FileElement(type, null)
        } else CompositeElement(type)
    }

    override fun createLeaf(type: IElementType, text: CharSequence): LeafElement {
        return when {
            JccTypesExt.CommentTypeSet.contains(type) -> {
                val default = ApplicationManager.getApplication().getService(DefaultASTFactory::class.java)
                default.createComment(type, text)
            }

            else                                      -> LeafPsiElement(type, text)
        }
    }
}
