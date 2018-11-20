package com.github.oowekyala.ijcc.lang.psi

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface JccRegularExpression : JavaccPsiElement {

    @JvmDefault
    fun asNamedRegularExpression(): JccNamedRegularExpression? = this as? JccNamedRegularExpression

}