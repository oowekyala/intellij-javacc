package com.github.oowekyala.ijcc.insight.model

import com.github.oowekyala.ijcc.insight.model.LexicalState.Companion.DefaultStateName
import com.github.oowekyala.ijcc.insight.model.LexicalState.Companion.LexicalStateBuilder
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
        buildStatesMap(grammarFileRoot?.childrenSequence()?.filter { it is JccBnfProduction || it is JccRegularExprProduction }
            ?: emptySequence())
    }

    /** All the tokens. TODO optimise s.t. it's not necessary to partition them by state. */
    val allTokens: Sequence<Token> by lazy {
        lexicalStates.flatMap { it.tokens }.asSequence()
    }

    val lexicalStates: Collection<LexicalState> = lexicalStatesMap.values

    fun getLexicalState(name: String): LexicalState? = lexicalStatesMap[name]

    companion object {

        private fun buildStatesMap(allProductions: Sequence<PsiElement>): Map<String, LexicalState> {

            val (regexpProductions, bnfProductions) =
                    allProductions.partition { it is JccRegularExprProduction }
                        .map({ it.filterIsInstance<JccRegularExprProduction>() },
                            { it.filterIsInstance<JccBnfProduction>() })

            val defaultBuilder = LexicalStateBuilder(DefaultStateName)

            // state name to builder
            val builders = mutableMapOf(
                // always add the default builder
                DefaultStateName to defaultBuilder
            )

            // productions applying to all states, processed when all states are known
            val applyToAll = mutableListOf<JccRegularExprProduction>()

            for (production in regexpProductions) {
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

            val allStateNames = builders.keys.toList()

            for (regexpProduction in applyToAll) {

                for (spec in regexpProduction.regexprSpecList) {
                    builders.values.asSequence().distinct().forEach { it.addToken(ExplicitToken(spec, allStateNames)) }
                }
            }

            val currentSpecs = builders.values.flatMap { it.currentSpecs }

            // deal with synthetic tokens
            for (bnfProduction in bnfProductions) {

                val regexpExpansions =
                        bnfProduction.expansion
                            ?.descendantSequence(includeSelf = true)
                            ?.filterIsInstance<JccRegexpExpansionUnit>()
                            ?: emptySequence()

                for (regexExpansion in regexpExpansions) {


                    val token = when (val r = regexExpansion.regularExpression.getRootRegexElement(false)) {

                        is JccLiteralRegexpUnit ->  // if the string isn't covered by an explicit token, it's synthesized
                            currentSpecs.firstOrNull { it.matchesLiteral(r) } ?: SyntheticToken(regexExpansion)

                        // necessarily references an explicit token
                        is JccTokenReferenceUnit -> null

                        // everything else is synthesized
                        else -> SyntheticToken(regexExpansion)
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