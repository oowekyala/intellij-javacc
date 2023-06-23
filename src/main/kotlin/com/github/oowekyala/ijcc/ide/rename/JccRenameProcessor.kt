package com.github.oowekyala.ijcc.ide.rename

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.intellij.openapi.util.Comparing
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.rename.UnresolvableCollisionUsageInfo
import com.intellij.usageView.UsageInfo


/**
 * TODO Correct behaviour would be:
 *   - renaming a production from a usage or from its decl
 *   changes the usages and adds a #Name decl with the previous
 *   JJTree name in JJTree grammars
 *   - renaming a JJTree annotation only changes other JJTree
 *   annotations and adds a #Name annotation on a production
 *   named the same if necessary, but doesn't change the name
 *   of the production nor impact its usages
 *
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccRenameProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean {
        if (element !is JccIdentifier) return false

        return element.parent.let {
            it is JccTokenReferenceRegexUnit
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

                processCollisions(key, value, file.globalNamedTokens, result) {
                    "A terminal named \'$it\' is already defined in this file"
                }

            } else {
                processCollisions(key, value, file.nonTerminalProductions, result) {
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
                    override fun getDescription(): String = description(newName)
                })
            }
        }
    }

    override fun findReferences(
        element: PsiElement,
        searchScope: SearchScope,
        searchInCommentsAndStrings: Boolean
    ): MutableCollection<PsiReference> {

        val file = element.containingFile

        val base: MutableCollection<PsiReference> = HashSet(
            ReferencesSearch.search(element, GlobalSearchScope.fileScope(file)).findAll()
        )

        if (element is JccIdentifier && (element.owner is JccJjtreeNodeDescriptor || element.owner is JccScopedExpansionUnit)) {
            (file as JccFile).getJjtreeDeclsForRawName(element.text).forEach {

                val eltToRename = when (it) {
                    is JccNonTerminalProduction -> when {
                        it.jjtreeNodeDescriptor == null               -> it
                        it.jjtreeNodeDescriptor?.name == element.name -> it.jjtreeNodeDescriptor
                        else                                          -> null
                    }
                    else                        -> it
                }
                eltToRename?.let {
                    base += ReferencesSearch.search(it, GlobalSearchScope.fileScope(file)).findAll()
                }
            }

        }

        return base
    }

    override fun prepareRenaming(element: PsiElement, // the JccIdentifier
                                 newName: String,
                                 allRenames: MutableMap<PsiElement, String>,
                                 scope: SearchScope) {

        // TODO also rename the Java PsiClass (but probably needs a ui agreement)
        // finds all node class owners with the same node class

        // add the non terminal prods in the map so that their usages are renamed
        // if they name the element themselves

//        allRenames.keys
//            .asSequence()
//            .mapNotNull { it as? JccIdentifier }
//            .filter { (it.owner as? JccNonTerminalProduction)?.nodeIdentifier == it }
//            ?.let {
//                allRenames[it.nameIdentifier] = newName
//            }
    }
}
