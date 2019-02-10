package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.ide.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

/**
 * Some counterpart to [LexicalGrammar] to get global insight about the productions
 * of a grammar. Will probably be fleshed out to implement lookahead inspections.
 *
 * @author Clément Fournier
 * @since 1.1
 */
class SyntaxGrammar(file: JccFile) {

    /**
     * Index of named productions by their name. This is used by [JccNonTerminalReference]
     * to resolve references quickly, which is probably nice for performance for
     * many large grammars.
     */
    private val namedTokensMap: Map<String, SmartPsiElementPointer<out JccNonTerminalProduction>> =
        file.nonTerminalProductions.associate { Pair(it.name, SmartPointerManager.createPointer(it)) }

    fun getProductionByName(name: String): JccNonTerminalProduction? = namedTokensMap[name]?.element

}