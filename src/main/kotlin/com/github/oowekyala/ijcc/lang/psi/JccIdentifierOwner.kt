package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

/**
 * An element that has an identifier.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccIdentifierOwner : JccPsiElement, PsiNameIdentifierOwner {

    override fun getNameIdentifier(): JccIdentifier?

    @JvmDefault
    override fun setName(name: String): PsiElement {
        nameIdentifier?.name = name
        return this
    }

}