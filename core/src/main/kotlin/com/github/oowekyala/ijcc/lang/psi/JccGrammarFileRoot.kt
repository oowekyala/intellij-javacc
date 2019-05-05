package com.github.oowekyala.ijcc.lang.psi

/**
 * Root node of the grammar. Used in place of [JccFile]
 * because the file element is not injectable. This allows
 * file-wide Java injection.
 *
 * Unless working with injection, [JccFile] provides more
 * utilities to work with the whole grammar, e.g. [JccFile.grammarOptions].
 *
 */
interface JccGrammarFileRoot : JccPsiElement {

    val optionSection: JccOptionSection?

    val parserDeclaration: JccParserDeclaration

}
