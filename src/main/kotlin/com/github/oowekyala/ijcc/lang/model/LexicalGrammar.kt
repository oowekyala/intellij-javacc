package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.model.LexicalState.Companion.DefaultStateName
import com.github.oowekyala.ijcc.lang.model.LexicalState.Companion.LexicalStateBuilder
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.runIt

/**
 * Represents the set of [LexicalState]s defined in a grammar file.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalGrammar(grammarFileRoot: JccGrammarFileRoot?) {

    /** All the defined lexical states. */
    private val lexicalStatesMap: Map<String, LexicalState> by lazy {
        buildStatesMap {
            grammarFileRoot?.childrenSequence()?.filterIsInstance<JccProduction>() ?: emptySequence()
        }
    }


    /** All the tokens. */
    val allTokens: Sequence<Token> by lazy {
        lexicalStates.flatMap { it.tokens }.asSequence()
    }

    val lexicalStates: Collection<LexicalState> = lexicalStatesMap.values

    fun getLexicalState(name: String): LexicalState? = lexicalStatesMap[name]

    fun getLexicalStates(namesOrEmptyForAll: Set<String>): Collection<LexicalState> =
        when {
            namesOrEmptyForAll.isEmpty() -> lexicalStates
            else                         -> lexicalStates.filter { namesOrEmptyForAll.contains(it.name) }
        }

    val defaultState: LexicalState
        get() = lexicalStatesMap.getValue(LexicalState.DefaultStateName)


    companion object {

        private fun buildStatesMap(allProductions: () -> Sequence<JccProduction>): Map<String, LexicalState> {

            // JavaCC collects all lexical state names during parser execution,
            // and only builds "lexical states" during the semanticise phase.
            // hence why we need two traversals here.
            val allLexicalStatesNames =
                allProductions().filterIsInstance<JccRegexProduction>()
                    .flatMap { it.lexicalStatesNameOrEmptyForAll.asSequence() }
                    .plus(DefaultStateName) // always there
                    .distinct()
                    .toList()

            // state name to builder
            val builders = allLexicalStatesNames.associateWith { name -> LexicalStateBuilder(name) }

            val defaultBuilder = builders.getValue(DefaultStateName)

            for (production in allProductions()) {

                when (production) {
                    is JccRegexProduction -> {
                        val rstates = production.lexicalStatesNameOrEmptyForAll

                        val relevantBuilders = when {
                            rstates.isEmpty()/* <*> */ -> builders.values
                            else                       -> rstates.map { builders.getValue(it) }
                        }

                        for (spec in production.regexSpecList) {
                            relevantBuilders.forEach { it.addToken(ExplicitToken(spec)) }
                        }
                    }

                    is JccBnfProduction   -> {

                        // all of those are put in the default state


                        val regexExpansions =
                            production.expansion
                                ?.descendantSequence(includeSelf = true)
                                ?.filterIsInstance<JccRegexExpansionUnit>()
                                ?: emptySequence()

                        val currentSpecs = defaultBuilder.currentSpecs


                        for (regexExpansion in regexExpansions) {


                            val token = when (val r = regexExpansion.regularExpression.getRootRegexElement(false)) {

                                is JccLiteralRegexUnit        ->  // if the string isn't covered by an explicit token, it's synthesized
                                    currentSpecs.firstOrNull { it.matchesLiteral(r) } // following references may cause infinite recursion
                                        ?: SyntheticToken(regexExpansion)

                                // necessarily references an explicit token
                                is JccTokenReferenceRegexUnit -> null

                                // everything else is synthesized
                                else                          -> SyntheticToken(regexExpansion)
                            } as? SyntheticToken

                            token?.runIt {
                                defaultBuilder.addToken(it)
                            }
                        }
                    }
                }
            }

            return builders.mapValues { (_, v) -> v.build() }
        }


    }

}