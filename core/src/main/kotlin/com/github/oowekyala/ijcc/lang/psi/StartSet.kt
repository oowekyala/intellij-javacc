package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.util.takeUntil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.immutableSetOf

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
 */
fun JccNonTerminalProduction.startSet(): Set<AtomicUnit> = when (this) {
    is JccBnfProduction -> computeStartSet(AlgoState())
    else                -> setOf(AtomicProduction(this))
}

/** Returns the start set of this expansion. */
fun JccExpansion.startSet(): Set<AtomicUnit> = startSet(AlgoState())

// The cache is local to a single analysis. It's here to speedup the
// algorithm and avoid quadratic explosion, but the start set shouldn't
// be cached in nodes.
private fun JccBnfProduction.computeStartSet(state: StartSetState): Set<AtomicUnit> =
    state.computeAndCache(this) { expansion?.startSet(it) ?: emptySet() }
    // if the method returns null, then this prod is in alreadySeen (left-recursion),
    // in which case we return the set of this
        ?: setOf(AtomicProduction(this))


private fun JccExpansion.startSet(state: StartSetState): Set<AtomicUnit> =
    when (this) {
        is JccRegexExpansionUnit         ->
            this.referencedToken?.let {
                immutableSetOf(AtomicToken(it))
            }   // then there's a token reference that couldn't be resolved
                ?: when (val r = this.regularExpression) {
                    is JccRefRegularExpression -> immutableSetOf(AtomicUnresolved(r))
                    else                       -> emptySet() // weird
                }

        is JccScopedExpansionUnit        -> expansionUnit.startSet(state)
        is JccAssignedExpansionUnit      -> assignableExpansionUnit?.startSet(state) ?: emptySet()
        is JccOptionalExpansionUnit      -> expansion?.startSet(state) ?: emptySet()
        is JccParenthesizedExpansionUnit -> expansion?.startSet(state) ?: emptySet()
        is JccTryCatchExpansionUnit      -> expansion?.startSet(state) ?: emptySet()
        is JccExpansionSequence          ->
            expansionUnitList.asSequence()
                .takeUntil { !it.isEmptyMatchPossible() }
                .map { it.startSet(state) }
                .fold(emptySet()) { a, b -> a + b }
        is JccExpansionAlternative       ->
            expansionList.asSequence()
                .map { it.startSet(state) }
                .fold(emptySet()) { a, b -> a + b }
        is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction()?.let {
            when (it) {
                is JccBnfProduction -> it.computeStartSet(state)
                else                -> setOf(AtomicProduction(it))
            }
        } ?: setOf(AtomicUnresolvedProd(this))
        // FIXME this is a parser bug, scoped exp unit is parsed as a raw expansion unit sometimes
        is JccExpansionUnit              ->
            childrenSequence()
                .mapNotNull { (it as? JccExpansionUnit)?.startSet(state) }
                .fold(emptySet()) { a, b -> a + b }
        else                             -> emptySet() // valid, but nothing to do
    }

private typealias StartSetState = AlgoState<JccNonTerminalProduction, Set<AtomicUnit>>

private class AlgoState<T : JccPsiElement, V>(
    // checks for left recursion
    val alreadySeen: ImmutableList<T> = immutableListOf(),
    val cache: MutableMap<T, V> = mutableMapOf()
) {
    fun computeAndCache(t: T, compute: T.(AlgoState<T, V>) -> V): V? {

        val existing: V? = cache[t]

        if (existing == null && t !in alreadySeen) {
            return t.compute(AlgoState(alreadySeen.add(t), cache))
        }

        return existing
    }
}

