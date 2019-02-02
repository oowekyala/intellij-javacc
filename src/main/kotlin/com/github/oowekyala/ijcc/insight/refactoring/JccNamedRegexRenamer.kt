package com.github.oowekyala.ijcc.insight.refactoring

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccNamedRegularExpression
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccNamedRegexRenamer : JccInPlaceRenameHandler() {
    override fun isAvailable(element: PsiElement?, editor: Editor, file: PsiFile): Boolean {
        val nameExpression = file.findElementForRename<JccIdentifier>(editor.caretModel.offset)

        return nameExpression != null && nameExpression.parent is JccNamedRegularExpression
    }
}


inline fun <reified T : PsiElement> PsiFile.findElementForRename(offset: Int): T? {
    return PsiTreeUtil.findElementOfClassAtOffset(this, offset, T::class.java, false)
        ?: PsiTreeUtil.findElementOfClassAtOffset(this, (offset - 1).coerceAtLeast(0), T::class.java, false)
}