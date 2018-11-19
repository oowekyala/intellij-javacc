package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaExpression : JavaccPsiElement, PsiLanguageInjectionHost {


    @JvmDefault
    override fun isValidHost(): Boolean = true

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost =
        this.replace(JccElementFactory.createJavaExpression(project, text)) as PsiLanguageInjectionHost

    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return LiteralTextEscaper.createSimple(this)
    }
}
