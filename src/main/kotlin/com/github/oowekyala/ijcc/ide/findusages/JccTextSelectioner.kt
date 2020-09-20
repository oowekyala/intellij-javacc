package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.owner
import com.intellij.codeInsight.hint.ImplementationTextSelectioner
import com.intellij.psi.PsiElement


class JccTextSelectioner : ImplementationTextSelectioner {
    override fun getTextEndOffset(elt: PsiElement): Int =
        (elt as? JccIdentifier)?.owner?.textRange?.endOffset
            ?: elt.textRange.endOffset


    override fun getTextStartOffset(element: PsiElement): Int =
        element.textRange.startOffset

}
