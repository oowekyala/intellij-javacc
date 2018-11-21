// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.reference.JccNonTerminalReference
import com.intellij.psi.PsiReference

interface JccNonTerminalExpansionUnit : JavaccPsiElement, JccExpansionUnit, JccIdentifierOwner {

    override fun getNameIdentifier(): JccIdentifier

    val javaExpressionList: JccJavaExpressionList


    @JvmDefault
    override fun getReference(): PsiReference? = JccNonTerminalReference(nameIdentifier)

}
