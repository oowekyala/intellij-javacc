package com.github.oowekyala.ijcc.ide.refs

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.manipulators.JccIdentifierManipulator
import com.github.oowekyala.ijcc.icons.JavaccIcons
import com.github.oowekyala.ijcc.util.asMap
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase


/**
 * Poly reference to the multiple declarations of a JJTree node.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JjtNodePolyReference(psiElement: JccNodeClassOwner)
    : PsiPolyVariantReferenceBase<JccNodeClassOwner>(psiElement) {

    override fun isReferenceTo(otherElt: PsiElement?): Boolean =
        otherElt is JccNodeClassOwner
            && otherElt.containingFile === element.containingFile
            && otherElt.isNotVoid
            && otherElt.rawName == element.rawName

    override fun multiResolve(incompleteCode: Boolean): Array<PsiEltResolveResult<JccNodeClassOwner>> {
        val myName = element.rawName ?: return emptyArray()

        return element.containingFile
            .syntaxGrammar
            .getJjtreeDeclsForRawName(myName)
            .asSequence()
            .mapNotNull { it.declarator }
            .map { PsiEltResolveResult(it) }
            .toList()
            .toTypedArray()
    }

    override fun getRangeInElement(): TextRange = element.nodeIdentifier!!.textRange.relativize(element.textRange)!!

    override fun handleElementRename(newElementName: String): PsiElement =
        JccIdentifierManipulator().handleContentChange(element.nodeIdentifier!!, newElementName)!!

    // reference completion is not used for those
    override fun getVariants(): Array<Any> = emptyArray()


    companion object {

        fun variantsForFile(jccFile: JccFile) =
            jccFile.syntaxGrammar
                .jjtreeNodes
                .asMap()
                .asSequence()
                .sortedBy { it.value.size }
                .mapNotNull { it.value.mapNotNull { it.declarator }.firstOrNull() }
                .mapNotNull { spec ->
                    val nodeName = spec.rawName ?: return@mapNotNull null
                    LookupElementBuilder.create(nodeName)
                        .withPresentableText("#$nodeName")
                        .withPsiElement(spec)
                        .withIcon(JavaccIcons.JJTREE_NODE)
                }
                .toList()
                .toTypedArray()
    }
}

