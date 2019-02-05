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
            buildStatesMap(grammarFileRoot?.childrenSequence()?.filter { it is JccBnfProduction || it is JccRegularExprProduction }
                ?: emptySequence())

    val lexicalStates: Collection<LexicalState> = lexicalStatesMap.values

    fun getLexicalState(name: String): LexicalState? = lexicalStatesMap[name]

    companion object {


        private fun buildStatesMap(allProductions: Sequence<PsiElement>): Map<String, LexicalState> {

            val (regexpProductions, nonterminalProductions) = allProductions.partition { it is JccRegularExprProduction }

            val defaultBuilder = LexicalStateBuilder(DefaultStateName)

            // state name to builder
            val builders = mutableMapOf(
                // always add the default builder
                DefaultStateName to defaultBuilder
            )

            // productions applying to all states, processed when all states are known
            val applyToAll = mutableListOf<JccRegularExprProduction>()

            for (production in regexpProductions.map { it as JccRegularExprProduction }) {
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

                for (spec in production.regexprSpecList) {
                    relevantBuilders.forEach { it.addToken(ExplicitToken(spec)) }
                }

            }


            for (regexpProduction in applyToAll) {

                for (spec in regexpProduction.regexprSpecList) {
                    builders.values.asSequence().distinct().forEach { it.addToken(ExplicitToken(spec)) }
                }
            }

            val currentSpecs = builders.values.flatMap { it.currentSpecs }

            for (bnfProduction in nonterminalProductions.mapNotNull { it as? JccBnfProduction }) {

                val regexpExpansions =
                        bnfProduction.expansion
                            ?.descendantSequence(includeSelf = true)
                            ?.filterMapAs<JccRegexpExpansionUnit>()
                            ?: emptySequence()

                for (regexExpansion in regexpExpansions) {

                    val token = when (val r = regexExpansion.regularExpression) {
                        is JccLiteralRegularExpression   ->
                            currentSpecs
                                .firstOrNull { it.matches(r.match) }
                            // if the string isn't covered by an explicit token, it's synthesized
                                ?: SyntheticToken(regexExpansion)
                        // necessarily references an explicit token
                        is JccRegularExpressionReference -> null
                        // everything else is synthesized
                        else                             -> SyntheticToken(regexExpansion)
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