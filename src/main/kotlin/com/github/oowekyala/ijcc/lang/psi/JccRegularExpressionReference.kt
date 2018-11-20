// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.reference.JccTerminalReference
import com.intellij.psi.PsiReference

interface JccRegularExpressionReference : JavaccPsiElement, JccRegularExpression {

    val identifier: JccIdentifier

    @JvmDefault
    override fun getReference(): PsiReference = JccTerminalReference(identifier)


}
