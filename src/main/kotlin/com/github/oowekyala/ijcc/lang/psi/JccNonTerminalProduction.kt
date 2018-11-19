package com.github.oowekyala.ijcc.lang.psi

/**
 * A non-terminal production.
 *
 * NonTerminalProduction ::= [JccBnfProduction] | [JccJavacodeProduction]
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccNonTerminalProduction : JccIdentifierOwner {

    @JvmDefault
    override fun getNameIdentifier(): JccIdentifier? = header.nameIdentifier

    val javaBlock: JccJavaBlock

    val header: JccNonTerminalProductionHeader

}