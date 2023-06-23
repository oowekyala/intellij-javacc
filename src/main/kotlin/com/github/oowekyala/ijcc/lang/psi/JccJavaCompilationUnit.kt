package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaCompilationUnit : PsiLanguageInjectionHost, JccPsiElement {
        override fun updateText(text: String): PsiLanguageInjectionHost =
        this.also {
            ElementManipulators.handleContentChange(this, text)
        }
//        this.replace(project.jccEltFactory.createJcu(text))
//            .let { it as PsiLanguageInjectionHost }
//            .also { HostSpec.replaceHost(this, it) }

        override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        MultilineTextEscaper(this)

        override fun isValidHost(): Boolean = true

}
