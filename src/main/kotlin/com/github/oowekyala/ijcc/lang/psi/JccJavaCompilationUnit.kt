// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaCompilationUnit : PsiLanguageInjectionHost, JavaccPsiElement {
    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost {
        return this.replace(JccElementFactory.createAcu(project, text)) as JccJavaCompilationUnit
    }

    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
            MultilineTextEscaper(this)

    @JvmDefault
    override fun isValidHost(): Boolean = false

}
