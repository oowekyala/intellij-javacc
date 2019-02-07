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

    // TODO optimisations that come to mind:
    // map of name to token, to resolve references!!
    // map of string to token, to resolve string tokens

    init {

        val onlyRef = mutableSetOf<Token>()
        val onlyString = mutableSetOf<Token>()
        val otherTokens = mutableSetOf<Token>()

        val nameToTokenMap = mutableMapOf<String, Token>()
        val indirectionMap = mutableMapOf<Token, String>()
        val tokenToStates = mutableMapOf<Token, List<String>>()

        // may contain tokens that JavaCC ignores, named tokens are never ignored
        val tokenToNameMap =
                grammarFileRoot.allProductions()
                    .flatMap { it.tokensUnfiltered() }
                    .associateWith { it.name }

        tokenToNameMap.forEach { (token, tokenName) ->

            val unit = token.regularExpression?.getRootRegexElement(followReferences = false)

            var refName: String? = null
            val lst = when (unit) {
                null                          -> null
                is JccLiteralRegexUnit        -> onlyString
                is JccTokenReferenceRegexUnit -> {
                    refName = unit.name
                    onlyRef
                }
                else                          -> otherTokens
            }

            lst?.add(token)

            // named tokens won't be removed. There may be duplicates, they're JavaCC errors anyway
            tokenName?.let { nameToTokenMap[it] = token }
            refName?.runIt { indirectionMap[token] = it }
            tokenToStates[token] = token.lexicalStatesOrEmptyForAll

        }

        val visited = mutableSetOf<Token>()
        /*
            Here we add check for tokens that are only references
            but reference (possibly indirectly) a string token. Those
            all are string tokens.
         */
        for (refToken in onlyRef) {

            if (refToken in visited) continue
            visited += refToken

            val indirectionPath = mutableListOf<Token>()

            var resolved: Token? = indirectionMap[refToken]?.let { nameToTokenMap[it] }

            while (
                resolved != null
                && indirectionMap.containsKey(resolved)
                && resolved !in indirectionPath  // avoid infinite loop caused by cyclic references
            ) {
                indirectionPath += resolved
                resolved = indirectionMap[resolved]?.let { nameToTokenMap[it] }
            }

            if (indirectionPath.isNotEmpty() && resolved in onlyString) {
                // then all the path are string tokens
                onlyString.addAll(indirectionPath)
                visited.addAll(indirectionPath)
            }
        }

        // Then we prune all string tokens that are duplicates


    }

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

        private fun JccGrammarFileRoot?.allProductions(): Sequence<JccProduction> =
                this?.childrenSequence()?.filterIsInstance<JccProduction>().orEmpty()

        private fun JccProduction.tokensUnfiltered(): Sequence<Token> {
            return when (this) {
                is JccRegexProduction -> regexSpecList.asSequence().map(::ExplicitToken)

                is JccBnfProduction   ->
                    expansion
                        ?.descendantSequence(includeSelf = true)
                        ?.filterIsInstance<JccRegexExpansionUnit>()
                        .orEmpty()
                        .map(::SyntheticToken)

                else                  -> emptySequence()
            }
        }


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
                                    currentSpecs.firstOrNull { it.matchesLiteral(r, followReferences = false) } // following references may cause infinite recursion
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