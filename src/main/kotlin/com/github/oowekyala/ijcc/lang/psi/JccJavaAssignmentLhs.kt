// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree
import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

interface JccJavaAssignmentLhs : JavaccPsiElement, PsiLanguageInjectionHost {

    val javaName: JccJavaName

    @JvmDefault
    override fun isValidHost(): Boolean = true

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost =
            this.replace(JccElementFactory.createJavaBlock(project, text))
                .let { it as PsiLanguageInjectionHost }
                .also { InjectionStructureTree.HostLeaf.replaceHost(this, it) }


    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return MultilineTextEscaper(this)
    }
}
