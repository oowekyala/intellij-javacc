// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaBlock : JavaccPsiElement, PsiLanguageInjectionHost {

    @JvmDefault
    override fun isValidHost(): Boolean = false

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost =
            this.replace(JccElementFactory.createJavaBlock(project, text)) as PsiLanguageInjectionHost

    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
            MultilineTextEscaper(this)
}
