
package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.injection.LinearInjectedStructure

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

    /**
     * A [LinearInjectedStructure] cached for this grammar.
     * File reparse causes another [LinearInjectedStructure] to
     * be created, which is very costly.
     */
    val linearInjectedStructure: LinearInjectedStructure

}
