// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.reference.JccStringTokenReference
import com.intellij.psi.PsiElement

interface JccLiteralRegularExpression : JccRegexpUnit, JccRegularExpression {

    val stringLiteral: PsiElement

    override fun getReference(): JccStringTokenReference?

}
