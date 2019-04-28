package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.lang.psi.*


/**
 * Return the next things to be done after this expansion is matched.
 * The analysis is local to the current expansion and doesn't expand
 * the next expansions.
 */
fun JccExpansion.step(): List<JccExpansionUnit> {

    val parent = parent

    return when {
        // ignoring parens
        this is JccParenthesizedExpansionUnit && parent is JccExpansion -> {
            parent.step()
        }
        // moving forwards
        parent is JccExpansionSequence                                  -> {
            val siblings = parent.expansionUnitList
            val myIdx = siblings.indexOf(this)
            if (myIdx < siblings.size - 1) siblings[myIdx + 1].firstStep() else parent.step()
        }
        // merging branches
        parent is JccExpansionAlternative                               -> parent.step()
        // nothing else to do
        else                                                            -> emptyList()
    }
}


/**
 * Returns the first step of this expansion
 */
fun JccExpansion.firstStep(): List<JccExpansionUnit> =
    when (this) {
        is JccParenthesizedExpansionUnit -> expansion?.firstStep() ?: emptyList()
        is JccExpansionAlternative       -> expansionList.flatMap { it.firstStep() }
        is JccExpansionSequence          -> expansionUnitList.first().firstStep()
        is JccLocalLookaheadUnit         -> (nextSiblingNoWhitespace as? JccExpansionUnit)?.firstStep() ?: emptyList()
        is JccExpansionUnit              -> listOf(this)
        else                             -> emptyList()
    }


