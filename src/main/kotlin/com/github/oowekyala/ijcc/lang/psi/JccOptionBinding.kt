// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.JccOptionType
import com.github.oowekyala.ijcc.model.JccOptionType.*
import com.intellij.psi.PsiElement

interface JccOptionBinding : JccIdentifierOwner {

    val integerLiteral: PsiElement?

    val stringLiteral: PsiElement?

    val booleanLiteral: JccBooleanLiteral?

    override fun getName(): String

    /**
     * Nullable because it could be e.g. a LOOKAHEAD token.
     * Use [getName] directly.
     */
    override fun getNameIdentifier(): JccIdentifier?

    /**
     * Returns null if the type is incorrect.
     */
    @JvmDefault
    fun <T : Any> getValue(expectedType: JccOptionType<T>): T? {
        val int = integerLiteral
        val string = stringLiteral
        val bool = booleanLiteral

        return when {
            int != null && expectedType == INTEGER   -> expectedType.parseStringValue(int.text)
            string != null && expectedType == STRING -> expectedType.parseStringValue(string.text.removeSurrounding("\""))
            bool != null && expectedType == BOOLEAN  -> expectedType.parseStringValue(bool.text)
            else                                     -> null
        }
    }

    /** Returns the string value for presentation. */
    val stringValue: String
        get() {
            val int = integerLiteral
            val string = stringLiteral
            val bool = booleanLiteral

            return when {
                int != null    -> int.text
                string != null -> string.text.removeSuffix("\"")
                bool != null   -> bool.text
                else           -> "??"
            }

        }

}
