package com.github.oowekyala.ijcc.lang.psi.impl

import com.github.oowekyala.ijcc.lang.psi.JavaccPsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface JccIdentifierTrait : PsiNamedElement, JavaccPsiElement {

    @JvmDefault
    override fun getName(): String? = text

    @JvmDefault
    override fun setName(name: String): PsiElement = replace(JccElementFactory.createIdentifier(project, name))

}