package com.github.oowekyala.ijcc.model

import com.github.oowekyala.ijcc.lang.psi.JccRegularExprProduction
import com.github.oowekyala.ijcc.lang.psi.modelConstant
import com.github.oowekyala.ijcc.model.LexicalState.Companion.DefaultStateName
import com.github.oowekyala.ijcc.model.LexicalState.Companion.LexicalStateBuilder

/**
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalGrammar(regexpProductions: Sequence<JccRegularExprProduction>) {


    /** All the defined lexical states. */
    private val lexicalStatesMap: Map<String, LexicalState> = buildStatesMap(regexpProductions)

    val lexicalStates: Collection<LexicalState> = lexicalStatesMap.values

    fun getLexicalState(name: String): LexicalState? = lexicalStatesMap[name]

    companion object {


        private fun buildStatesMap(allProductions: Sequence<JccRegularExprProduction>): Map<String, LexicalState> {
            // state name to builder
            val builders = mutableMapOf<String, LexicalStateBuilder>()

            // productions applying to all states, processed when all states are known
            val applyToAll = mutableListOf<JccRegularExprProduction>()

            for (regexpProduction in allProductions) {
                val rstates = regexpProduction.lexicalStateList


                val relevantBuilders: List<LexicalStateBuilder> = if (rstates == null) {
                    // DEFAULT
                    listOf(builders.getOrPut(DefaultStateName) { LexicalStateBuilder(DefaultStateName) })
                } else if (rstates.identifierList.isEmpty()) {
                    // <*>
                    applyToAll += regexpProduction
                    continue // processed at the end, when all lexical states are known
                } else {
                    val names = rstates.identifierList.map { it.name }
                    names.map { builders.getOrPut(it) { LexicalStateBuilder(it) } }
                }

                val kind = regexpProduction.regexprKind.modelConstant

                for (spec in regexpProduction.regexprSpecList) {
                    val token = Token(kind, spec)
                    relevantBuilders.forEach { it.addToken(token) }
                }
            }

            for (regexpProduction in applyToAll) {

                val kind = regexpProduction.regexprKind.modelConstant

                for (spec in regexpProduction.regexprSpecList) {
                    val token = Token(kind, spec)
                    builders.values.asSequence().distinct().forEach { it.addToken(token) }
                }
            }

            return builders.mapValues { (_, v) -> v.build() }
        }


    }

}