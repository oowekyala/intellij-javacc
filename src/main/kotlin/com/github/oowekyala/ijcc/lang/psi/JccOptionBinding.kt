// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.insight.model.GenericOption
import com.github.oowekyala.ijcc.insight.model.JccOptionType

interface JccOptionBinding : JccIdentifierOwner {

    val optionValue: JccOptionValue?

    override fun getName(): String

    /**
     * Nullable because it could be e.g. a LOOKAHEAD token.
     * Use [getName] directly.
     */
    override fun getNameIdentifier(): JccIdentifier?

    @JvmDefault
    val modelOption: GenericOption<*>?
        get() = GenericOption.knownOptions[name]


    /** Returns true if types match. */
    @JvmDefault
    fun <T : Any> matchesType(expectedType: JccOptionType<T>): Boolean =
            expectedType.projection == optionValue?.optionType

    /** Returns the string value for presentation. */
    @JvmDefault
    val stringValue: String
        get() = optionValue?.stringValue ?: ""

}
