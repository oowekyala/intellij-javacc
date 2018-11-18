package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * @author Clément Fournier
 * @since 1.0
 */
interface JccJavaCUInjectionHost : PsiLanguageInjectionHost, JavaccPsiElement {

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost {
        return this.replace(JccElementFactory.createAcu(project, text)) as JccJavaCompilationUnit
    }

    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return LiteralTextEscaper.createSimple(this)
    }

    @JvmDefault
    override fun isValidHost(): Boolean = true


}