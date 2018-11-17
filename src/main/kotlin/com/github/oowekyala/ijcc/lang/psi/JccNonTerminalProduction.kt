package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

/**
 * A non-terminal production.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccNonTerminalProduction : JavaccPsiElement, PsiNamedElement {

    @JvmDefault
    override fun getName(): String? {
        return "foo"
    }

    @JvmDefault
    override fun setName(name: String): PsiElement {
        return this
    }


}