package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
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
    fun toJavaMethodHeader(): String = text

    @JvmDefault
    override fun isValidHost(): Boolean = true

    @JvmDefault
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
            MultilineTextEscaper(this)

    @JvmDefault
    override fun updateText(text: String): PsiLanguageInjectionHost =
            this.replace(JccElementFactory.createJavaNonterminalHeader(project, text)) as PsiLanguageInjectionHost


}
