// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.reference.JccStringTokenReference
import com.intellij.psi.PsiElement
import com.intellij.psi.util.strictParents

/**
 * A literal regular expression. Holds a reference only if it's not in a regexp spec.
 * TODO this could be improved to match tokens declared further up
 */
interface JccLiteralRegularExpression : JccRegexpUnit {

    val stringLiteral: PsiElement

    override fun getReference(): JccStringTokenReference? =
            JccStringTokenReference(this)
                .takeUnless { _ -> strictParents().any { it is JccRegexprSpec } }

}
