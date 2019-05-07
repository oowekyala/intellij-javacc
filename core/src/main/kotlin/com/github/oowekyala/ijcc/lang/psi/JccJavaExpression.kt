package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.HostSpec
import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaExpression : JccPsiElement, PsiLanguageInjectionHost {


    @JvmDefault
    override fun isValidHost(): Boolean = true

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost =
        this.replace(project.jccEltFactory.createJavaExpression(text))
            .let { it as PsiLanguageInjectionHost }
            .also { HostSpec.replaceHost(this, it) }


    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        MultilineTextEscaper(this)
}
