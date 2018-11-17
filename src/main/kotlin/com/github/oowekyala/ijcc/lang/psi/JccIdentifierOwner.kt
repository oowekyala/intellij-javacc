package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface JccIdentifierOwner : PsiNameIdentifierOwner, JavaccPsiElement {

    val nameIdentifier: JccIdentifier?

    @JvmDefault
    override fun getName(): String? = nameIdentifier?.name

    @JvmDefault
    override fun setName(name: String): PsiElement {
        nameIdentifier?.setName(name)
        return this
    }

}