package com.github.oowekyala.ijcc.icons

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.intellij.ide.IconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccIconProvider : IconProvider(), DumbAware {
    override fun getIcon(element: PsiElement, flags: Int): Icon? = when (element) {
        is JccFile -> getFileIcon(element)
        else       -> null
    }

    private fun getFileIcon(file: JccFile): Icon? = when (file.grammarNature) {
        GrammarNature.JJTREE -> JccIcons.JJTREE_FILE
        GrammarNature.JAVACC -> JccIcons.JAVACC_FILE
    }

}