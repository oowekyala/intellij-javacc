package com.github.oowekyala.ijcc.lang.psi

/**
 * A non-terminal production.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccNonTerminalProduction : JccIdentifierOwner {

    override val nameIdentifier: JccIdentifier?
        get() = children.first { it is JccIdentifier } as? JccIdentifier


}