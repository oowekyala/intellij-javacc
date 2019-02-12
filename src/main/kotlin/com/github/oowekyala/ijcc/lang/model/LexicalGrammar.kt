package com.github.oowekyala.ijcc.lang.model

import com.github.oowekyala.ijcc.ide.refs.JccTerminalReference
import com.github.oowekyala.ijcc.lang.model.LexicalState.Companion.DefaultStateName
import com.github.oowekyala.ijcc.lang.model.LexicalState.Companion.LexicalStateBuilder
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.associateByToMostlySingular
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.psi.SmartPointerManager
import com.intellij.util.containers.MostlySingularMultiMap

/**
 * Represents the set of [LexicalState]s defined in a grammar file.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class LexicalGrammar(file: JccFile) {

    // Initialization order of this object is fragile

    // The initialization routine of buildStatesMap indirectly uses
    // JccTerminalReference (through matchesLiteral), which itself relies
    // on the LexicalGrammar of the file to resolve token names quickly
    // with the index below.

    // So by the time buildStatesMap is invoked, the constructor must have
    // returned, otherwise JccFileImpl never assigns its LexicalGrammar
    // and will ask for a new one (infinite recursion).

    // So we must be careful with laziness and delegated properties. In particular,
    // do take care of not using property initializers that call the lazy
    // properties! That would force their evaluation

    // Though it's error-prone, the rest apart from JccTerminalReference and this
    // object the rest of the app need not care, which is nice.

    /**
     * Index of named tokens by their name. This is used by [JccTerminalReference]
     * to resolve references quickly, which is crucial to performance on some
     * large grammars.
     */
    private val namedTokensMap: MostlySingularMultiMap<String, Token> =
        file.tokensUnfiltered()
            .filter { it.regularExpression is JccNamedRegularExpression }
            .associateByToMostlySingular { it.name!! }


    /** All the defined lexical states. */
    private val lexicalStatesMap: Map<String, LexicalState> by lazy {
        buildStatesMap { file.allProductions() }
    }


    /** All the tokens. */
    val allTokens: Sequence<Token> by lazy {
        lexicalStates.flatMap { it.tokens }.asSequence()
    }

    val lexicalStates: Collection<LexicalState>
        get() = lexicalStatesMap.values


    val defaultState: LexicalState
        get() = lexicalStatesMap.getValue(LexicalState.DefaultStateName)

    /**
     * Returns the first token that has the given name. It's unique in well-formed
     * grammars, but malformed ones may contain duplicates.
     */
    fun getTokenByName(name: String): Token? = namedTokensMap.get(name).firstOrNull()

    /**
     * Returns the tokens that have the given name. It's unique in well-formed
     * grammars, but malformed ones may contain duplicates.
     */
    fun getTokenByNameMulti(name: String): List<Token> = namedTokensMap.get(name).toList()

    /**
     * Returns the lexical state that goes by this name. [defaultState] is
     * a shortcut to get the default lexical state, which is always present.
     */
    fun getLexicalState(name: String): LexicalState? = lexicalStatesMap[name]

    fun getLexicalStates(namesOrEmptyForAll: Set<String>): Collection<LexicalState> =
        when {
            namesOrEmptyForAll.isEmpty() -> lexicalStates
            else                         -> lexicalStates.filter { namesOrEmptyForAll.contains(it.name) }
        }

    companion object {


        private fun buildStatesMap(allProductions: () -> Sequence<JccProduction>): Map<String, LexicalState> {

            // JavaCC collects all lexical state names during parser execution,
            // and only builds "lexical states" during the semanticise phase.
            // hence why we need two traversals here.

            // state name to builder
            val builders = initBuilders(allProductions)

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

                            val regex = regexExpansion.regularExpression

                            if (regex is JccEofRegularExpression) continue // doesn't generate a token

                            val token = when (val r = regex.getRootRegexElement(false)) {

                                is JccLiteralRegexUnit        ->  // if the string isn't covered by an explicit token, it's synthesized
                                    currentSpecs.firstOrNull { it.matchesLiteral(r) } ?: SyntheticToken(regexExpansion)

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

        private fun initBuilders(allProductions: () -> Sequence<JccProduction>): Map<String, LexicalStateBuilder> =
            allProductions().filterIsInstance<JccRegexProduction>()
                .flatMap { it.lexicalStatesIdents.asSequence() }
                .distinct()
                .toList()
                .associateTo(mutableMapOf()) { id ->
                    Pair(id.name, LexicalStateBuilder(id.name, SmartPointerManager.createPointer(id)))
                }
                .also { it.computeIfAbsent(DefaultStateName) { name -> LexicalStateBuilder(name, null) } }

        /**
         * Returns a stream of all "potential" tokens in a grammar. String tokens
         * should be reduced, but this is only done within [buildStatesMap]. This is only
         * provided to have a quick and dirty way to build a cache of named tokens,
         * which won't be reduced.
         */
        private fun JccFile.tokensUnfiltered(): Sequence<Token> {

            fun JccProduction.tokensUnfiltered(): Sequence<Token> {
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
            return allProductions().flatMap { it.tokensUnfiltered() }
        }

    }

}