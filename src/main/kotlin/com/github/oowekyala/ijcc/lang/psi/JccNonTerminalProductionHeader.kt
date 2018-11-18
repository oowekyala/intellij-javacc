package com.github.oowekyala.ijcc.lang.psi

/**
 * Header of a [JccNonTerminalProduction]
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccNonTerminalProductionHeader : JavaccPsiElement, JccIdentifierOwner {
    val javaParameterList: JccJavaParameterList

}