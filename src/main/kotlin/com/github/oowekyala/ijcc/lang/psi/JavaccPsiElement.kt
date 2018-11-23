package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * Any javacc psi element.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JavaccPsiElement : NavigatablePsiElement {

    override fun getContainingFile(): JccFile

    /**
     * True if the node is incomplete (due to eg a pin).
     * In that case every non-null accessor may return
     * null so the node should be ignored.
     */
    @JvmDefault
    val isBroken
        get() = false
}