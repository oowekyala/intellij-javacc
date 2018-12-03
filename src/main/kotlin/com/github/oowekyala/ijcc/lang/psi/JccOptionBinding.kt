// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.JccOptionType
import com.github.oowekyala.ijcc.insight.model.JccOptionType.BaseOptionType
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

    val valueNode
        get() = listOf(integerLiteral, stringLiteral, booleanLiteral).first { it != null }

    /**
     * Returns false if the type is incorrect.
     */
    fun <T : Any> matchesType(expectedType: JccOptionType<T>): Boolean {
        val int = integerLiteral
        val string = stringLiteral
        val bool = booleanLiteral

        return when {
            int != null    -> expectedType.projection == BaseOptionType.INTEGER
            string != null -> expectedType.projection == BaseOptionType.STRING
            bool != null   -> expectedType.projection == BaseOptionType.BOOLEAN
            else           -> false
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
                string != null -> string.text.removeSurrounding("\"")
                bool != null   -> bool.text
                else           -> "??"
            }

        }

}
