package com.github.oowekyala.ijcc.lang.refs

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.github.oowekyala.ijcc.util.filterMapAs
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult


/**
 * Poly reference to the multiple declarations of a JJTree node.
 *
 * @param myId  Mostly used to force the caller to ascertain that it's not null
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JjtNodePolyReference(psiElement: JccNodeClassOwner, private val myId: JccIdentifier)
    : PsiPolyVariantReferenceBase<JccNodeClassOwner>(psiElement) {

    override fun getVariants(): Array<Any> = emptyArray()

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val myName = element.nodeSimpleName ?: return emptyArray()

        return element.containingFile
            .nonTerminalProductions
            .flatMap { it.descendantSequence(includeSelf = true) }
            .filterMapAs<JccNodeClassOwner>()
            .filter { it.nodeSimpleName == myName }
            .map { PsiElementResolveResult(it) }
            .toList().toTypedArray()
    }

    override fun getRangeInElement(): TextRange = myId.textRange.relativize(element.textRange)!!

    override fun handleElementRename(newElementName: String): PsiElement =
            JccIdentifierManipulator().handleContentChange(myId, newElementName)!!

}