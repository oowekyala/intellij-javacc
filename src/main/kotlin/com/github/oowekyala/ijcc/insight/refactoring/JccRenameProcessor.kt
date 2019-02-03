package com.github.oowekyala.ijcc.insight.refactoring

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.openapi.util.Comparing
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.rename.UnresolvableCollisionUsageInfo
import com.intellij.usageView.UsageInfo


/**
 * @author Clément Fournier
 * @since 1.0
 */
object JccRenameProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        if (element !is JccIdentifier) return false

        return element.parent.let {
            it is JccTokenReferenceUnit
                    || it is JccNonTerminalExpansionUnit
                    || it is JccNamedRegularExpression
                    || it is JccJavaNonTerminalProductionHeader
                    || it is JccJjtreeNodeDescriptor
        }
    }

    private fun isTerminal(psiElement: PsiElement): Boolean? {
        val elt =
                if (psiElement is JccIdentifier)
                    PsiTreeUtil.findFirstParent(psiElement) { it is JccIdentifierOwner }
                else psiElement as? JccIdentifierOwner

        return elt is JccNamedRegularExpression
    }


    override fun findCollisions(
        element: PsiElement,
        newName: String,
        allRenames: Map<out PsiElement, String>,
        result: MutableList<UsageInfo>
    ) {

        allRenames.forEach { key, value ->
            val file = key.containingFile as JccFileImpl
            if (isTerminal(key) == true) {

                processCollisions(
                    key,
                    value,
                    file.globalNamedTokens,
                    result
                ) {
                    "A terminal named \'$it\' is already defined in this file"
                }

            } else {
                processCollisions(
                    key,
                    value,
                    file.nonTerminalProductions,
                    result
                ) {
                    "A production named \'$it\' is already defined in this file"
                }
            }
        }
    }

    private fun processCollisions(element: PsiElement,
                                  newName: String,
                                  sameKind: Sequence<PsiNamedElement>,
                                  result: MutableList<UsageInfo>,
                                  description: (String) -> String) {
        for (spec in sameKind) {
            if (Comparing.strEqual(newName, spec.name)) {
                result.add(object : UnresolvableCollisionUsageInfo(spec, element) {
                    override fun getDescription(): String {
                        return description(newName)
                    }
                })
            }
        }
    }

    override fun prepareRenaming(element: PsiElement, // the JccIdentifier
                                 newName: String,
                                 allRenames: MutableMap<PsiElement, String>,
                                 scope: SearchScope) {

        // TODO also rename the Java PsiClass (but probably needs a ui agreement)
        // finds all node class owners with the same node class
        element.parent.parent
            .let { it as? JccNodeClassOwner }
            // only continue if the renamed element is the ident of the node
            // can be different for productions with an annotation
            ?.takeIf { it.nodeIdentifier == element }
            ?.typedReference
            ?.multiResolve(incompleteCode = false)
            ?.runIt { allRenames.putAll(it.associate { Pair(it.element.nodeIdentifier!!, newName) }) }
    }
}