// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.util.prevSiblingNoWhitespace

interface JccNamedRegularExpression : JccRegularExpression, JccIdentifierOwner {

    val regularExpression: JccRegularExpression

    override fun getNameIdentifier(): JccIdentifier

    /**
     * Returns true if this regex is private, ie if it mentions the "#" before its name.
     * Such regular expressions may not be referred to from expansion units, but only
     * from within other regular expressions.  Private regular expressions are not matched
     * as tokens by the token manager. Their purpose is solely to facilitate the definition
     * of other more complex regular expressions.
     */
    @JvmDefault
    val isPrivate: Boolean
        get() = nameIdentifier.prevSiblingNoWhitespace.node.elementType == JavaccTypes.JCC_POUND

}
