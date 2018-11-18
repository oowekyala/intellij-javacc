package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface JccIdentifierOwner : PsiNameIdentifierOwner, JavaccPsiElement {

    val nameIdentifier: JccIdentifier?
        get() = children.first { it is JccIdentifier } as? JccIdentifier

    override fun getNameIdentifier(): PsiElement? = nameIdentifier

    override fun getName(): String? = nameIdentifier?.name

    override fun setName(name: String): PsiElement {
        nameIdentifier?.name = name
        return this
    }

}