package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.MultilineTextEscaper
import com.github.oowekyala.ijcc.lang.psi.impl.jccEltFactory
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * Header of a non-terminal production, containing the java parts.
 */
interface JccJavaNonTerminalProductionHeader : JccIdentifierOwner, PsiLanguageInjectionHost {

    val javaFormalParameterList: List<JccJavaFormalParameter>

    val javaReturnType: JccJavaReturnType

    val javaThrowsList: JccJavaThrowsList?

    val javaAccessModifier: JccJavaAccessModifier

    override fun getNameIdentifier(): JccIdentifier

        fun toJavaMethodHeader(): String = text

        override fun isValidHost(): Boolean = false

        override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        MultilineTextEscaper(this)

        override fun updateText(text: String): PsiLanguageInjectionHost =
        this.replace(project.jccEltFactory.createJavaNonterminalHeader(text)) as PsiLanguageInjectionHost


}
