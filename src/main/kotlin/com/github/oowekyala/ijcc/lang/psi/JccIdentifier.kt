package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface JccIdentifier : JccPsiElement, PsiNamedElement {

    override fun getName(): String

    val leaf: PsiElement
}
