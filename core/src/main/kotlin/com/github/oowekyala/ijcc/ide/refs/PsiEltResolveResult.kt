package com.github.oowekyala.ijcc.ide.refs

import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult

data class PsiEltResolveResult<out T : PsiElement>(private val myElt: T) :
    ResolveResult {
    override fun getElement(): T = myElt

    override fun isValidResult(): Boolean = true
}