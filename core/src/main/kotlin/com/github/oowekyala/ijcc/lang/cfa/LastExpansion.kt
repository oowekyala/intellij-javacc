package com.github.oowekyala.ijcc.lang.cfa

import com.github.oowekyala.ijcc.lang.psi.JccExpansion
import com.github.oowekyala.ijcc.lang.psi.JccExpansionSequence
import com.intellij.psi.PsiElement


fun JccExpansion.isNextStep(bound: PsiElement): Boolean {
    val parent = parent

    return when {
        this == bound                  -> true
        parent is JccExpansionSequence -> parent.expansionUnitList.last() == this && parent.isNextStep(bound)
        parent is JccExpansion         -> parent.isNextStep(bound)
        else                           -> parent == bound
    }
}
