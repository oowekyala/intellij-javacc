package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.github.oowekyala.ijcc.lang.psi.parentSequence
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccDocUtil {

    @Suppress("UNUSED_PARAMETER")
    fun findLinkTarget(psiManager: PsiManager?, ref: String?, context: PsiElement?): PsiElement? {
        val psiFile = context?.containingFile as? JccFile ?: return null
        val (type, name) = ref?.split("/") ?: return null

        return when (type) {
            "token"       -> psiFile.globalNamedTokens
            "nonterminal" -> psiFile.nonTerminalProductions
            else          -> emptySequence()
        }.filter { it.name == name }.firstOrNull()
    }

    fun getLinkRefTo(jccPsiElement: PsiElement?): String? {

        val relevantParent = jccPsiElement?.parentSequence(includeSelf = true)
            ?.first { it is JccRegexprSpec || it is JccNonTerminalProduction }
            ?: return null

        return when (relevantParent) {
            is JccRegexprSpec           -> "token/${relevantParent.name}"
            is JccNonTerminalProduction -> "nonterminal/${relevantParent.name}"
            else                        -> null
        }
    }


}