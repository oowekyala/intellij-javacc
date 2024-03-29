package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase


/**
 * Poly reference to the multiple declarations of a JJTree node.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JjtNodePolyReference(psiElement: JjtNodeClassOwner)
    : PsiPolyVariantReferenceBase<JjtNodeClassOwner>(psiElement) {

    override fun isReferenceTo(otherElt: PsiElement): Boolean =
        otherElt is JjtNodeClassOwner
            && otherElt.containingFile === element.containingFile
            && otherElt.isNotVoid
            && otherElt.nodeRawName == element.nodeRawName

    /**
     * Like [multiResolve] but doesn't wrap the results in [PsiEltResolveResult]
     * and an array. Returned elements always have a name.
     */
    fun lightMultiResolve(): List<JjtNodeClassOwner> =
        element.nodeRawName?.let {
            element.containingFile.getJjtreeDeclsForRawName(it)
        }.orEmpty()

    override fun multiResolve(incompleteCode: Boolean): Array<PsiEltResolveResult<JjtNodeClassOwner>> =
        lightMultiResolve()
            .map { PsiEltResolveResult(it) }
            .toList()
            .toTypedArray()

    override fun getRangeInElement(): TextRange = element.nodeIdentifier!!.textRange.relativize(element.textRange)!!

    override fun handleElementRename(newElementName: String): PsiElement =
        JccIdentifierManipulator().handleContentChange(element.nodeIdentifier!!, newElementName)!!

    override fun getVariants(): Array<Any> =
        JccRefVariantService.getInstance(element.project).jjtreeNodeVariants(this)

}

