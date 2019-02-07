package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.lang.model.LexicalState.Companion.DefaultStateName
import com.github.oowekyala.ijcc.lang.model.LexicalState.Companion.LexicalStateBuilder
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.map
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
    private val lexicalStatesMap: Map<String, LexicalState> by lazy {
        buildStatesMap(grammarFileRoot?.childrenSequence()?.filter { it is JccBnfProduction || it is JccRegexProduction }
            ?: emptySequence())
    }

    /** All the tokens. TODO optimise s.t. it's not necessary to partition them by state. */
    val allTokens: Sequence<Token> by lazy {
        lexicalStates.flatMap { it.tokens }.asSequence()
    }

    val lexicalStates: Collection<LexicalState> = lexicalStatesMap.values

    fun getLexicalState(name: String): LexicalState? = lexicalStatesMap[name]

    fun getDefaultState() : LexicalState = lexicalStatesMap[LexicalState.DefaultStateName]!!

    companion object {

        private fun buildStatesMap(allProductions: Sequence<PsiElement>): Map<String, LexicalState> {

            val (regexProductions, bnfProductions) =
                    allProductions.partition { it is JccRegexProduction }
                        .map({ it.filterIsInstance<JccRegexProduction>() },
                            { it.filterIsInstance<JccBnfProduction>() })

            val defaultBuilder = LexicalStateBuilder(DefaultStateName)

            // state name to builder
            val builders = mutableMapOf(
                // always add the default builder
                DefaultStateName to defaultBuilder
            )

            // productions applying to all states, processed when all states are known
            val applyToAll = mutableListOf<JccRegexProduction>()

            for (production in regexProductions) {
                val rstates = production.lexicalStateList

                val relevantBuilders: List<LexicalStateBuilder> = if (rstates == null) {
                    // DEFAULT
                    listOf(defaultBuilder)
                } else if (rstates.identifierList.isEmpty()) {
                    // <*>
                    applyToAll += production
                    continue // processed at the end, when all lexical states are known
                } else {
                    val names = rstates.identifierList.map { it.name }
                    names.map { builders.getOrPut(it) { LexicalStateBuilder(it) } }
                }

                for (spec in production.regexSpecList) {
                    relevantBuilders.forEach { it.addToken(ExplicitToken(spec)) }
                }

            }

            for (regexProduction in applyToAll) {

                for (spec in regexProduction.regexSpecList) {
                    builders.values.asSequence().distinct().forEach { it.addToken(ExplicitToken(spec)) }
                }
            }

            val currentSpecs = builders.values.flatMap { it.currentSpecs }

            // deal with synthetic tokens
            for (bnfProduction in bnfProductions) {

                val regexExpansions =
                        bnfProduction.expansion
                            ?.descendantSequence(includeSelf = true)
                            ?.filterIsInstance<JccRegexExpansionUnit>()
                            ?: emptySequence()

                for (regexExpansion in regexExpansions) {


                    val token = when (val r = regexExpansion.regularExpression.getRootRegexElement(false)) {

                        is JccLiteralRegexUnit                                           ->  // if the string isn't covered by an explicit token, it's synthesized
                            currentSpecs.firstOrNull { it.matchesLiteral(r) } ?: SyntheticToken(regexExpansion)

                        // necessarily references an explicit token
                        is JccTokenReferenceRegexUnit -> null

                        // everything else is synthesized
                        else                                                             -> SyntheticToken(regexExpansion)
                    } as? SyntheticToken

                    token?.runIt {
                        defaultBuilder.addToken(it)
                    }
                }
            }

            return builders.mapValues { (_, v) -> v.build() }
        }


    }

}