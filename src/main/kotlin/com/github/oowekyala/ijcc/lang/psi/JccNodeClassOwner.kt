package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.model.JavaccConfig
import com.intellij.psi.NavigatablePsiElement

/**
 * Node that is tied to a concrete generated node class.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccNodeClassOwner {

    fun nodeClass(javaccConfig: JavaccConfig): NavigatablePsiElement?

}