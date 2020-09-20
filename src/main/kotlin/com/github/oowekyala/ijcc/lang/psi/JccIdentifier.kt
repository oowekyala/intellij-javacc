package com.github.oowekyala.ijcc.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface JccIdentifier : JccPsiElement, PsiNamedElement {

    override fun getName(): String

    val leaf: PsiElement
}

val JccIdentifier.owner: JccIdentifierOwner?
    get() =
        ancestors(includeSelf = false)
            .takeWhile { it is JccIdentifierOwner }
            .filterIsInstance<JccIdentifierOwner>()
            .lastOrNull { it.nameIdentifier == this }


val JccIdentifier.isJjtreeNodeIdentifier: Boolean
    get() = parent is JccJjtreeNodeDescriptor
        || (parent as? JccJavaNonTerminalProductionHeader)
        ?.let { it.parent as JccNonTerminalProduction }
        ?.let { it.nameIdentifier == it.nodeIdentifier } == true

val JccIdentifier.isLexicalStateName: Boolean
    get() = parent is JccLexicalStateList || parent is JccRegexSpec