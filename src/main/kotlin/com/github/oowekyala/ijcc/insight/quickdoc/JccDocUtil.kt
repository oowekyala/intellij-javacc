package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccRegexprSpec
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccDocUtil {

    const val LeftRightStart = "<p style='text-align:left;'>"
    const val LeftRightMiddle = "<span style='float:right;'>"
    const val LeftRightEnd = "</span></p>"

    private const val TerminalRef = "token"
    private const val NonterminalRef = "nonterminal"


    /** Finds the target of a link created by [getLinkRefTo]. */
    @Suppress("UNUSED_PARAMETER")
    fun findLinkTarget(psiManager: PsiManager?, ref: String?, context: PsiElement?): PsiElement? {
        val psiFile = context?.containingFile as? JccFile ?: return null
        val (type, name) = ref?.split("/") ?: return null

        return when (type) {
            TerminalRef    -> psiFile.globalNamedTokens
            NonterminalRef -> psiFile.nonTerminalProductions
            else           -> emptySequence()
        }.filter { it.name == name }.firstOrNull()
    }

    /** Gets a hyperlink suitable for use with [DocumentationManager.createHyperlink]. */
    fun getLinkRefTo(spec: JccRegexprSpec): String = "$TerminalRef/${spec.name}"

    /** Gets a hyperlink suitable for use with [DocumentationManager.createHyperlink]. */
    fun getLinkRefTo(production: JccNonTerminalProduction): String = "$NonterminalRef/${production.name}"


    fun emph(it: String) = "<i>$it</i>"
    fun bold(it: String) = "<b>$it</b>"
    fun angles(it: String) = "&lt;$it&gt;"


}