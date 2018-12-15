package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface JccIdentifier : JavaccPsiElement, PsiNamedElement {

    override fun getName(): String

    @JvmDefault
    override fun setName(name: String): PsiElement = replace(JccElementFactory.createIdentifier(project, name))

}
