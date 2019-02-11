package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.ide.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.lang.psi.*
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

    // TODO indexing like that is supposed to be done by stub indices


    /**
     * Index of named productions by their name. This is used by [JccNonTerminalReference]
     * to resolve references quickly, which is probably nice for performance for
     * many large grammars.
     */
    private val productionsByName: MostlySingularMultiMap<String, ModelProduction> =
        file.nonTerminalProductions
            .map(::ModelProduction)
            .associateByToMostlySingular { it.name }

    private val jjtreeNodesByName: MostlySingularMultiMap<String, JjtreeNodeSpec> =
        file.nonTerminalProductions
            .flatMap { it.descendantSequence(includeSelf = true) }
            .filterIsInstance<JccNodeClassOwner>()
            .filter { it.isNotVoid }
            .map(::JjtreeNodeSpec)
            .associateByToMostlySingular { it.nodeRawName }


    fun getProductionByName(name: String): JccNonTerminalProduction? =
        productionsByName.get(name).firstOrNull()?.nonTerminal

    fun getProductionByNameMulti(name: String): List<ModelProduction> = productionsByName.get(name).toList()

    /**
     * Gets the partial declarations of the jjtree node with this name declared in this file.
     * The name has to be the [JccNodeClassOwner.rawName].
     */
    fun getJjtreeDeclsForRawName(name: String): List<JjtreeNodeSpec> = jjtreeNodesByName.get(name).toList()

    val jjtreeNodes: MostlySingularMultiMap<String, JjtreeNodeSpec> = jjtreeNodesByName
}


data class JjtreeNodeSpec(val pointer: SmartPsiElementPointer<out JccNodeClassOwner>) {

    constructor(decl: JccNodeClassOwner) : this(SmartPointerManager.createPointer(decl))

    val declarator: JccNodeClassOwner? = pointer.element
    val nodeSimpleName: String? get() = declarator?.nodeSimpleName
    val nodeRawName: String? get() = declarator?.rawName
}

data class ModelProduction(val name: String, val pointer: SmartPsiElementPointer<out JccNonTerminalProduction>) {

    constructor(production: JccNonTerminalProduction)
        : this(production.name, SmartPointerManager.createPointer(production))

    val nonTerminal: JccNonTerminalProduction? = pointer.element
}