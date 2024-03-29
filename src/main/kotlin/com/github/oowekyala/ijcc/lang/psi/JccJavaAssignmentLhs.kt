package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.HostSpec
import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaAssignmentLhs : JccPsiElement, PsiLanguageInjectionHost {

    val javaName: JccJavaName

        override fun isValidHost(): Boolean = true

        override fun updateText(text: String): PsiLanguageInjectionHost =
        this.replace(project.jccEltFactory.createAssignmentLhs(text))
            .let { it as PsiLanguageInjectionHost }
            .also { HostSpec.replaceHost(this, it) }


        override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        MultilineTextEscaper(this)
}
