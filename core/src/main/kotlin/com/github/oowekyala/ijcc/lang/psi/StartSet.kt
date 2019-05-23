package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.util.takeUntil
import com.intellij.openapi.util.Key
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
 * Returns true if this production can expand to the empty string.
 */
fun JccNonTerminalProduction.startSet(): Set<AtomicUnit> = when (this) {
    is JccBnfProduction -> computeStartSet(immutableListOf())
    else                -> setOf(AtomicProduction(this))
}

fun JccExpansion.startSet(): Set<AtomicUnit> = startSet(immutableListOf())

private fun JccBnfProduction.computeStartSet(alreadySeen: ImmutableList<JccBnfProduction>): Set<AtomicUnit> =
    computeAndCache(alreadySeen) { expansion?.startSet(it) ?: emptySet() }
    // if the method returns null, then this prod is in alreadySeen (left-recursion),
    // in which case we return the set of this
        ?: setOf(AtomicProduction(this))


private fun JccExpansion.startSet(alreadySeen: ImmutableList<JccBnfProduction>): Set<AtomicUnit> =
    when (this) {
        is JccRegexExpansionUnit         ->
            this.referencedToken?.let {
                immutableSetOf(AtomicToken(it))
            }   // then there's a token reference that couldn't be resolved
                ?: when (val r = this.regularExpression) {
                    is JccRefRegularExpression -> immutableSetOf(AtomicUnresolved(r))
                    else                       -> emptySet() // weird
                }

        is JccScopedExpansionUnit        -> expansionUnit.startSet(alreadySeen)
        is JccAssignedExpansionUnit      -> assignableExpansionUnit?.startSet(alreadySeen) ?: emptySet()
        is JccOptionalExpansionUnit      -> expansion?.startSet(alreadySeen) ?: emptySet()
        is JccParenthesizedExpansionUnit -> expansion?.startSet(alreadySeen) ?: emptySet()
        is JccTryCatchExpansionUnit      -> expansion?.startSet(alreadySeen) ?: emptySet()
        is JccExpansionSequence          ->
            expansionUnitList.asSequence()
                .takeUntil { !it.isEmptyMatchPossible() }
                .map { it.startSet(alreadySeen) }
                .fold(emptySet()) { a, b -> b + a } // b + a to use the add of immutableSet
        is JccExpansionAlternative       ->
            expansionList.asSequence()
                .map { it.startSet(alreadySeen) }
                .fold(emptySet()) { a, b -> b + a } // b + a to use the add of immutableSet
        is JccNonTerminalExpansionUnit   -> typedReference.resolveProduction()?.let {
            when (it) {
                is JccBnfProduction -> it.computeStartSet(alreadySeen)
                else                -> setOf(AtomicProduction(it))
            }
        } ?: setOf(AtomicUnresolvedProd(this))
        // FIXME this is a parser bug, scoped exp unit is parsed as a raw expansion unit sometimes
        is JccExpansionUnit              ->
            childrenSequence()
                .mapNotNull { (it as? JccExpansionUnit)?.startSet(alreadySeen) }
                .fold(emptySet()) { a, b -> b + a }
        else                             -> immutableSetOf() // valid, but nothing to do
    }


private val startSetKey: Key<Set<AtomicUnit>> = Key.create("jcc.bnf.leftMostSet")


private fun <T : JccPsiElement> T.computeAndCache(
    alreadySeen: ImmutableList<T>,
    compute: T.(ImmutableList<T>) -> Set<AtomicUnit>
): Set<AtomicUnit>? {

    val existing: Set<AtomicUnit>? = getUserData(startSetKey)

    if (existing == null && this !in alreadySeen) {
        val computed = this.compute(alreadySeen.add(this))
        putUserData(startSetKey, computed)
        return computed
    }

    return existing
}
