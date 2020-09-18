package com.github.oowekyala.ijcc.lang.cfa

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccExpansionSequence
import com.intellij.psi.PsiElement

/**
 * Given that [bound] is an ancestor of this expansion,
 * finds out if [this] is executed last when executing
 * the [bound]. This is used to find the last expansion
 * of a node scope.
 */
fun JccExpansion.isNextStep(bound: PsiElement): Boolean {
    val parent = parent

    return when {
        this == bound                  -> true
        parent is JccExpansionSequence -> parent.expansionUnitList.last() == this && parent.isNextStep(bound)
        parent is JccExpansion         -> parent.isNextStep(bound)
        else                           -> parent == bound
    }
}
