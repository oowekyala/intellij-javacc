package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.JavaccLanguage
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType

/**
 * Root of all Javacc files.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccFile : PsiFile, JccPsiElement, PsiClassOwner {

    /** The unique parser declaration of this file. */
    val parserDeclaration: JccParserDeclaration?

    /** All non-terminal productions. */
    val nonTerminalProductions: Sequence<JccNonTerminalProduction>

    /** All terminal productions. */
    val regexProductions: Sequence<JccRegexProduction>

    /** First mention of a lexical state name, conventionally treated as its declaration. */
    val lexicalStatesFirstMention: Sequence<JccIdentifier>

    /** The injectable file root. */
    val grammarFileRoot: JccGrammarFileRoot?

    /** Information about lexical states and defined tokens. */
    val lexicalGrammar: LexicalGrammar

    /**
     * Named regexes of the TOKEN kind defined globally in the file.
     * May contain private regexes.
     */
    val globalNamedTokens: Sequence<JccNamedRegularExpression>

    /** Regex specs of the TOKEN kind defined globally in the file. Superset of [globalNamedTokens]. */
    val globalTokenSpecs: Sequence<JccRegexSpec>

    /** Options section. */
    val options: JccOptionSection?


    val tokenManagerDecls: Sequence<JccTokenManagerDecls>

    /**
     * Returns true if the file is conventionally named *.jjt,
     * or it uses JJTree options or node descriptors in the code.
     */
    val grammarNature: GrammarNature

    companion object {
        /** Element type. */
        val TYPE = IFileElementType("JCC_FILE", JavaccLanguage)
    }
}