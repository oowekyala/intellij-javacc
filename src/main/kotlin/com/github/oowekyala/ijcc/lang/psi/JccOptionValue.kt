// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.JccOptionType
import com.intellij.psi.PsiElement

interface JccOptionValue : JavaccPsiElement {

    val booleanLiteral: JccBooleanLiteral?

    val integerLiteral: PsiElement?

    val stringLiteral: PsiElement?


    @JvmDefault
    val optionType: JccOptionType.BaseOptionType<*>
        get() {
            val int = integerLiteral
            val string = stringLiteral
            val bool = booleanLiteral

            return when {
                int != null    -> JccOptionType.BaseOptionType.INTEGER
                string != null -> JccOptionType.BaseOptionType.STRING
                bool != null   -> JccOptionType.BaseOptionType.BOOLEAN
                else           -> throw IllegalStateException()
            }
        }

    /** Returns the string value for presentation. */
    @JvmDefault
    val stringValue: String
        get() {
            val int = integerLiteral
            val string = stringLiteral
            val bool = booleanLiteral

            return when {
                int != null    -> int.text
                string != null -> string.text.removeSurrounding("\"")
                bool != null   -> bool.text
                else           -> "??"
            }
        }
}
