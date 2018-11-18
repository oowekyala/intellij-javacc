package com.github.oowekyala.ijcc.lang.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * @author Clément Fournier
 * @since 1.0
 */
class JccProductionReference(psiElement: PsiElement, textRange: TextRange)
    : PsiReferenceBase<PsiElement>(psiElement, textRange) {

    override fun resolve(): PsiElement? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVariants(): Array<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}