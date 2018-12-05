// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

interface JccOptionBinding : JccIdentifierOwner {

    val optionValue: JccOptionValue?

    override fun getName(): String

    /**
     * Nullable because it could be e.g. a LOOKAHEAD token.
     * Use [getName] directly.
     */
    override fun getNameIdentifier(): JccIdentifier?

}