package com.github.oowekyala.ijcc.lang.cfa

import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.foldNullable
import com.github.oowekyala.ijcc.util.takeUntil
import kotlinx.collections.immutable.*
import kotlin.math.max

/**
 * A unit of a FIRST set. This can be a token, a production (either if we hit
 * left-recursion, or if we group productions that produce at most one token),
 * or an unresolved token or production reference.
 */
sealed class AtomicUnit

/** Valid token. */
data class AtomicToken(val token: Token) : AtomicUnit()

/** Unresolved token. */
data class AtomicUnresolved(val ref: JccRefRegularExpression) : AtomicUnit()

/** Unresolved production. */
data class AtomicUnresolvedProd(val ref: JccNonTerminalExpansionUnit) : AtomicUnit()

/** Either JAVACODE, or left-recursive, we won't dig further. */
data class AtomicProduction(val production: JccNonTerminalProduction) : AtomicUnit()

/**
 * Returns the start set of this production. Unresolved references and Javacode
 * productions are considered opaque and hence atomic.
 *
 * If [groupUnary] is true, then productions that yield at most one token are considered
 * atomic. So if you have a production A := "b" | "c" in the FIRST set, instead of having
 * "b" and "c" in the resulting set, you have A.
 */
fun JccNonTerminalProduction.firstSet(groupUnary: Boolean = false): Set<AtomicUnit> = when (this) {
    is JccBnfProduction -> computeFirst(
        FirstSetStateImpl(
            groupUnary = groupUnary
        )
    )
    else                                                   -> setOf(
        AtomicProduction(
            this
        )
    )
}

/** Returns the first set of this expansion. */
fun JccExpansion.firstSet(groupUnary: Boolean = false): Set<AtomicUnit> =
    firstSetImpl(FirstSetStateImpl(groupUnary = groupUnary))

// The cache is local to a single analysis. It's here to speedup the
// algorithm and avoid quadratic explosion, but the start set shouldn't
// be cached in nodes.
private fun JccBnfProduction.computeFirst(state: FirstSetState): Set<AtomicUnit> =
    state.computeAndCache(this) { expansion?.firstSetImpl(it) ?: emptySet() }
    // if the method returns null, then this prod is in alreadySeen (left-recursion),
    // in which case we return the set of this
        ?: setOf(AtomicProduction(this))


private fun JccExpansion.firstSetImpl(state: FirstSetState): Set<AtomicUnit> =
    when (this) {
        is JccLocalLookaheadUnit         -> emptySet()

        is JccRegexExpansionUnit         ->
            this.referencedToken?.let { persistentSetOf(AtomicToken(it)) }
            // if null, then there's a token reference that couldn't be resolved
                ?: when (val r = this.regularExpression) {
                    is JccRefRegularExpression -> persistentSetOf(AtomicUnresolved(r))
                    else                       -> emptySet() // weird
                }

        is JccScopedExpansionUnit        -> expansionUnit.firstSetImpl(state)
        is JccAssignedExpansionUnit      -> assignableExpansionUnit?.firstSetImpl(state) ?: emptySet()
        is JccOptionalExpansionUnit      -> expansion?.firstSetImpl(state) ?: emptySet()
        is JccParenthesizedExpansionUnit -> expansion?.firstSetImpl(state) ?: emptySet()
        is JccTryCatchExpansionUnit      -> expansion?.firstSetImpl(state) ?: emptySet()
        is JccExpansionSequence          ->
            expansionUnitList.asSequence()
                .takeUntil { !it.isEmptyMatchPossible() }
                .map { it.firstSetImpl(state) }
                .fold(emptySet()) { a, b -> a + b }
        is JccExpansionAlternative       ->
            expansionList.asSequence()
                .map { it.firstSetImpl(state) }
                .fold(emptySet()) { a, b -> a + b }
        is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction()?.let {
            when (it) {
                is JccBnfProduction -> it.computeFirst(state)
                else                                                   -> setOf(
                    AtomicProduction(
                        it
                    )
                )
            }
        } ?: setOf(AtomicUnresolvedProd(this))
        else                                                                -> emptySet() // valid, but nothing to do
    }


private typealias FirstSetState = AlgoState<JccNonTerminalProduction, Set<AtomicUnit>>

private class FirstSetStateImpl(
    alreadySeen: PersistentList<JccNonTerminalProduction> = persistentListOf(),
    cache: MutableMap<JccNonTerminalProduction, Set<AtomicUnit>> = mutableMapOf(),
    /**
     * Whether to consider productions that can only ever match a single token
     * atomic.
     */
    val groupUnary: Boolean,
    /**
     * We use a cache of max tokens too.
     */
    private val maxTokensState: MaxTokensState = AlgoState()
) : FirstSetState(alreadySeen, cache) {


    override fun next(t: JccNonTerminalProduction): FirstSetState =
        FirstSetStateImpl(alreadySeen.add(t), cache, groupUnary, maxTokensState)

    override fun computeAndCache(t: JccNonTerminalProduction,
                                 compute: JccNonTerminalProduction.(FirstSetState) -> Set<AtomicUnit>): Set<AtomicUnit>? {

        val sup = super.computeAndCache(t, compute) ?: return null
        if (!groupUnary) return sup

        val isAtomic = t.maxTokens(maxTokensState)?.let { it <= 1 } ?: false

        return if (isAtomic) {
            setOf(AtomicProduction(t)).also {
                cache[t] = it
            }
        } else {
            sup
        }
    }

}


// TODO reuse this for regex start sets
private open class AlgoState<T : JccPsiElement, V>(
    // checks for left recursion
    val alreadySeen: PersistentList<T> = persistentListOf(),
    val cache: MutableMap<T, V> = mutableMapOf()
) {

    open fun next(t: T): AlgoState<T, V> =
        AlgoState(alreadySeen.add(t), cache)

    open fun computeAndCache(t: T, compute: T.(AlgoState<T, V>) -> V): V? {

        val existing: V? = cache[t]

        if (existing == null && t !in alreadySeen) {
            return t.compute(next(t)).also { cache[t] = it }
        }

        return existing
    }
}

private typealias MaxTokensState = AlgoState<JccNonTerminalProduction, Int?>

/**
 * Finds out the length of the longest match possible of this expansion.
 * If null, then the longest match is unbounded.
 */
private fun JccNonTerminalProduction.maxTokens(state: MaxTokensState): Int? =
    when (this) {
        is JccBnfProduction -> state.computeAndCache(this) { st ->
            expansion.let {
                if (it == null) 0 else it.maxTokens(st)
            }
        }
        else                                                   -> null
    }

/**
 * Null represents "unbounded" or unknown
 */
private fun JccExpansion.maxTokens(state: MaxTokensState): Int? =
    when (this) {
        is JccLocalLookaheadUnit         -> 0
        is JccParserActionsUnit          -> 0
        is JccRegexExpansionUnit         -> 1
        is JccScopedExpansionUnit        -> expansionUnit.maxTokens(state)
        is JccAssignedExpansionUnit      ->
            assignableExpansionUnit?.maxTokens(state)
                ?: 0 // no expansion is 0, we mustn't hide the null
        is JccParenthesizedExpansionUnit ->
            when (occurrenceIndicator) {
                is JccZeroOrMore,
                is JccOneOrMore -> null

                else            ->
                    // no expansion is 0, we mustn't hide the null
                    expansion?.maxTokens(state) ?: 0
            }
        is JccExpansionSequence          ->
            expansionUnitList.asSequence()
                .map { it.maxTokens(state) }.foldNullable(0) { a, b -> a + b }
        is JccExpansionAlternative       ->
            expansionList.asSequence()
                .map { it.maxTokens(state) }.foldNullable(0) { a, b -> max(a, b) }
        is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction().let {
            when (it) {
                is JccBnfProduction -> it.maxTokens(state)
                else                                                   -> null
            }
        }
        is JccTryCatchExpansionUnit      -> expansion?.maxTokens(state)
        else                                                                -> 0
    }
