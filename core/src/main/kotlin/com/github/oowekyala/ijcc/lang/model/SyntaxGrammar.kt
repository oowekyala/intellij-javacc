package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.ide.refs.JccNonTerminalReference
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.ijcc.util.asMap
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
class SyntaxGrammar(file: JccFile) : BaseCachedModelObject(file) {

    // TODO indexing like that is supposed to be done by stub indices


    /**
     * Index of named productions by their name. This is used by [JccNonTerminalReference]
     * to resolve references quickly, which is probably nice for performance for
     * many large grammars.
     */
    private val productionsByName: MostlySingularMultiMap<String, ProductionPointer> =
        file.nonTerminalProductions
            .associateByToMostlySingular({ it.name }) {
                it
            }

    private val jjtreeNodesByName: MostlySingularMultiMap<String, JjtreeNodePointer> =
        file.nonTerminalProductions
            .flatMap { it.descendantSequence(includeSelf = true) }
            .filterIsInstance<JjtNodeClassOwner>()
            .filter { it.isNotVoid }
            .associateByToMostlySingular({ it.nodeRawName }) {
                it
            }

    val allJjtreeNodes: Map<String, List<JjtNodeClassOwner>>
        get() = jjtreeNodesByName.asMap().mapValues { (_, v) -> v.map { it } }


    fun getProductionByNameMulti(name: String): List<JccNonTerminalProduction> =
        productionsByName.get(name).mapNotNullTo(mutableListOf()) { it }

    /**
     * Gets the partial declarations of the jjtree node with this name declared in this file.
     * The name has to be the [JjtNodeClassOwner.nodeRawName].
     */
    fun getJjtreeDeclsForRawName(name: String): List<JjtNodeClassOwner> =
        jjtreeNodesByName.get(name).mapNotNullTo(mutableListOf()) { it }

    //
    //    val usesJjtreeDescriptors: Boolean by lazy {
    //        jjtreeNodes
    //            .asMap()
    //            .values
    //            .asSequence()
    //            .flatten().any { it.declarator?.nodeIdentifier?.parent is JccJjtreeNodeDescriptor }
    //    }
}

private typealias JjtreeNodePointer = JjtNodeClassOwner
private typealias ProductionPointer = JccNonTerminalProduction
