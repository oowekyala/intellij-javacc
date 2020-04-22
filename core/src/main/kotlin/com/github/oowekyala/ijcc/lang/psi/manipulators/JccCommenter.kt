package com.github.oowekyala.ijcc.lang.psi.manipulators

import com.github.oowekyala.ijcc.lang.JccTypes
import com.intellij.lang.CodeDocumentationAwareCommenterEx
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

object JccCommenter : CodeDocumentationAwareCommenterEx {
    override fun getLineCommentPrefix(): String? = "//"

    override fun getBlockCommentPrefix(): String? = "/*"

    override fun getBlockCommentSuffix(): String? = "*/"

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null

    override fun getLineCommentTokenType(): IElementType? = JccTypes.JCC_END_OF_LINE_COMMENT

    override fun getBlockCommentTokenType(): IElementType? = JccTypes.JCC_C_STYLE_COMMENT

    // TODO javadoc:

    override fun getDocumentationCommentTokenType(): IElementType? = null

    override fun getDocumentationCommentPrefix(): String? = "/**"

    override fun getDocumentationCommentLinePrefix(): String? = "*"

    override fun getDocumentationCommentSuffix(): String? = "*/"

    override fun isDocumentationComment(element: PsiComment): Boolean = false

    override fun isDocumentationCommentText(element: PsiElement): Boolean = false
}
