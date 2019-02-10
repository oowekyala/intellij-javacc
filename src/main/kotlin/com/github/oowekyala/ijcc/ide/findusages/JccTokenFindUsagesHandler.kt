package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope


object JccStringTokenFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler? =
        when (element) {
            is JccLiteralRegexUnit ->
                element.typedReference!!
                    .resolveToken(exact = true)
                    ?.let { JccTokenFindUsagesHandler(it.psiElement!!) }

            is JccIdentifier       -> JccTokenFindUsagesHandler(element.namedTokenDef!!)
            else                   -> null
        }

    override fun canFindUsages(element: PsiElement): Boolean =
        element is JccLiteralRegexUnit && element.typedReference != null
            || element is JccIdentifier && element.namedTokenDef != null

}

class JccTokenFindUsagesHandler(regexOwner: JccRegularExpressionOwner) : FindUsagesHandler(regexOwner) {

    override fun findReferencesToHighlight(target: PsiElement,
                                           searchScope: SearchScope): MutableCollection<PsiReference> {
        val realTarget = when (target) {
            is JccIdentifier -> target.namedTokenDef!!
            else             -> target
        }

        return super.findReferencesToHighlight(realTarget, searchScope)
    }
}