// This is a generated file. Not intended for manual editing.
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * Header of a non-terminal production, containing the java parts.
 */
interface JccJavaNonTerminalProductionHeader : JccIdentifierOwner, PsiLanguageInjectionHost {

    val javaParameterList: JccJavaParameterList

    val javaReturnType: JccJavaReturnType

    val javaThrowsList: JccJavaThrowsList?

    override fun getNameIdentifier(): JccIdentifier

    @JvmDefault
    override fun isValidHost(): Boolean = true

    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return LiteralTextEscaper.createSimple(this)
    }

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost =
        this.replace(JccElementFactory.createJavaNonterminalHeader(project, text)) as PsiLanguageInjectionHost


}
