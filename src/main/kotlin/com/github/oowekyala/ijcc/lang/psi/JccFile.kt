package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.model.LexicalGrammar
import com.github.oowekyala.ijcc.lang.model.SyntaxGrammar
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiFile

/**
 * Root of all Javacc files.
 *
 * @author Clément Fournier
 * @since 1.0
 */
interface JccFile : PsiFile, JccPsiElement, PsiClassOwner
{

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

    val syntaxGrammar: SyntaxGrammar

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


    val grammarNature: GrammarNature
}


val JccFile.defaultLexStateName: String
    get() = grammarOptions.inlineBindings.defaultLexicalState21

val JccFile.allJjtreeDecls: Map<String, List<JjtNodeClassOwner>>
    get() = (this as JccFileImpl).syntaxGrammar.allJjtreeNodes


fun JccFile.getJjtreeDeclsForRawName(name: String): List<JjtNodeClassOwner> =
    (this as JccFileImpl).syntaxGrammar.getJjtreeDeclsForRawName(name)

fun JccFile.getProductionByName(name: String): JccNonTerminalProduction? =
    getProductionByNameMulti(name).firstOrNull()

fun JccFile.getProductionByNameMulti(name: String): List<JccNonTerminalProduction> =
    (this as JccFileImpl).syntaxGrammar.getProductionByNameMulti(name)

fun JccFile.allProductions(): Sequence<JccProduction> =
    grammarFileRoot?.childrenSequence()?.filterIsInstance<JccProduction>().orEmpty()
