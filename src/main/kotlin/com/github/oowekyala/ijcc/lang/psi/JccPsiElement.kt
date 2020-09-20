package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.intellij.psi.NavigatablePsiElement

/**
 * Top-level interface for all javacc psi element.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccPsiElement : NavigatablePsiElement {

    override fun getContainingFile(): JccFile

    /** Gets the options bundle associated with the grammar this element is found in. */
    @JvmDefault
    val grammarOptions: IGrammarOptions
        get() = containingFile.grammarOptions


}
