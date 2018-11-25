package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.model.LexicalGrammar
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType

/**
 * Root of all Javacc files.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccFile : PsiFile, JavaccPsiElement {

    /** The unique parser declaration of this file. */
    val parserDeclaration: JccParserDeclaration

    /** All non-terminal productions. */
    val nonTerminalProductions: Sequence<JccNonTerminalProduction>

    /** All terminal productions. */
    val regexpProductions: List<JccRegularExprProduction>

    val lexicalGrammar: LexicalGrammar

    /**
     * Named regexes of the TOKEN kind defined globally in the file.
     * May contain private regexes.
     */
    val globalNamedTokens: Sequence<JccNamedRegularExpression>
    /**
     * Named regexes of the TOKEN kind defined globally in the file.
     * Doesn't contain private regexes.
     */
    val globalPublicNamedTokens: Sequence<JccNamedRegularExpression>

    /** Regexpr specs of the TOKEN kind defined globally in the file. Superset of [globalNamedTokens]. */
    val globalTokenSpecs: Sequence<JccRegexprSpec>

    /** Options section. */
    val options: JccOptionSection?

    companion object {
        /** Element type. */
        val TYPE = IFileElementType("JCC_FILE", JavaccLanguage)
    }
}