package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.model.Token
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
                    ?.let { JccTokenFindUsagesHandler(it) }
            is JccIdentifier       -> {
                val token = element.namedTokenDef!!.definedToken
                JccTokenFindUsagesHandler(token)
            }
            else                   -> null
        }

    override fun canFindUsages(element: PsiElement): Boolean =
        element is JccLiteralRegexUnit && element.typedReference != null // a non-null typed refere
            || element is JccIdentifier && element.namedTokenDef != null

}

class JccTokenFindUsagesHandler(val token: Token) : FindUsagesHandler(token.psiElement!!) {
    override fun getPrimaryElements(): Array<PsiElement> = listOfNotNull(token.psiElement).toTypedArray()


    override fun findReferencesToHighlight(target: PsiElement,
                                           searchScope: SearchScope): MutableCollection<PsiReference> {
        val realTarget = when (target) {
            is JccIdentifier -> target.namedTokenDef!!
            else             -> target
        }

        return super.findReferencesToHighlight(realTarget, searchScope)
    }
}