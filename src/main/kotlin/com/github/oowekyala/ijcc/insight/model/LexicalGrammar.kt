package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.insight.model.LexicalState.Companion.DefaultStateName
import com.github.oowekyala.ijcc.insight.model.LexicalState.Companion.LexicalStateBuilder
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.filterMapAs
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.psi.PsiElement

/**
 * Represents the set of [LexicalState]s defined in a grammar file.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalGrammar(grammarFileRoot: JccGrammarFileRoot?) {


    /** All the defined lexical states. */
    private val lexicalStatesMap: Map<String, LexicalState> =
            buildStatesMap(grammarFileRoot?.childrenSequence() ?: emptySequence())

    val lexicalStates: Collection<LexicalState> = lexicalStatesMap.values

    fun getLexicalState(name: String): LexicalState? = lexicalStatesMap[name]

    companion object {


        private fun buildStatesMap(allProductions: Sequence<PsiElement>): Map<String, LexicalState> {
            // state name to builder
            val builders = mutableMapOf<String, LexicalStateBuilder>()

            // productions applying to all states, processed when all states are known
            val applyToAll = mutableListOf<JccRegularExprProduction>()

            for (production in allProductions) {
                if (production is JccRegularExprProduction) {
                    val rstates = production.lexicalStateList


                    val relevantBuilders: List<LexicalStateBuilder> = if (rstates == null) {
                        // DEFAULT
                        listOf(builders.getOrPut(DefaultStateName) { LexicalStateBuilder(DefaultStateName) })
                    } else if (rstates.identifierList.isEmpty()) {
                        // <*>
                        applyToAll += production
                        continue // processed at the end, when all lexical states are known
                    } else {
                        val names = rstates.identifierList.map { it.name }
                        names.map { builders.getOrPut(it) { LexicalStateBuilder(it) } }
                    }

                    for (spec in production.regexprSpecList) {
                        relevantBuilders.forEach { it.addToken(ExplicitToken(spec)) }
                    }

                } else if (production is JccBnfProduction) {

                    val defaultBuilder = builders.getOrPut(DefaultStateName) { LexicalStateBuilder(DefaultStateName) }

                    val regexpExpansions =
                            production.expansion
                                ?.descendantSequence(includeSelf = true)
                                ?.filterMapAs<JccRegexpExpansionUnit>()
                                ?: emptySequence()
                    for (regex in regexpExpansions) {

                        val synthetic = regex.referencedToken as? SyntheticToken

                        synthetic?.runIt {
                            defaultBuilder.addToken(it)
                        }
                    }
                }

            }

            for (regexpProduction in applyToAll) {

                for (spec in regexpProduction.regexprSpecList) {
                    builders.values.asSequence().distinct().forEach { it.addToken(ExplicitToken(spec)) }
                }
            }

            return builders.mapValues { (_, v) -> v.build() }
        }


    }

}