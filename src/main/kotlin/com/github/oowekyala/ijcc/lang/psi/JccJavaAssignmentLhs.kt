package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.HostSpec
import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaAssignmentLhs : JccPsiElement, PsiLanguageInjectionHost {

    val javaName: JccJavaName

    @JvmDefault
    override fun isValidHost(): Boolean = true

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost =
        this.replace(JccElementFactory.createAssignmentLhs(project, text))
            .let { it as PsiLanguageInjectionHost }
            .also { HostSpec.replaceHost(this, it) }


    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return MultilineTextEscaper(this)
    }
}
