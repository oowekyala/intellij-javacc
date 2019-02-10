package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.ide.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.util.associateByToMostlySingular
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.containers.MostlySingularMultiMap

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
    private val namedTokensMap: MostlySingularMultiMap<String, ModelProduction> =
        file.nonTerminalProductions
            .map(::ModelProduction)
            .associateByToMostlySingular { it.name }


    fun getProductionByName(name: String): JccNonTerminalProduction? =
        namedTokensMap.get(name).firstOrNull()?.nonTerminal

    fun getProductionByNameMulti(name: String): List<ModelProduction> = namedTokensMap.get(name).toList()


    data class ModelProduction(val name: String, val pointer: SmartPsiElementPointer<out JccNonTerminalProduction>) {

        constructor(production: JccNonTerminalProduction)
            : this(production.name, SmartPointerManager.createPointer(production))

        val nonTerminal: JccNonTerminalProduction? = pointer.element
    }
}