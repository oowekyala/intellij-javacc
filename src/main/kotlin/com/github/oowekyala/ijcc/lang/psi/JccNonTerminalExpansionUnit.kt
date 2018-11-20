// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.reference.JccNonTerminalReference
import com.intellij.psi.PsiReference

interface JccNonTerminalExpansionUnit : JavaccPsiElement {

    val identifier: JccIdentifier

    val javaExpressionList: JccJavaExpressionList


    @JvmDefault
    override fun getReference(): PsiReference? = JccNonTerminalReference(identifier)

}
