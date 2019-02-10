package com.github.oowekyala.ijcc.ide.findusages

import com.github.oowekyala.ijcc.lang.model.Token
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.psi.PsiElement


object JccStringTokenFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler? =
        when (element) {
            is JccLiteralRegexUnit ->
                element.typedReference!!
                    .resolveToken(exact = true)
                    ?.let { JccStringTokenFindUsagesHandler(it, element) }
            is JccIdentifier       -> {
                val token = element.namedTokenDef!!.definedToken
                JccStringTokenFindUsagesHandler(token, null)
            }
            else                   -> null
        }


    override fun canFindUsages(element: PsiElement): Boolean =
        element is JccLiteralRegexUnit && element.typedReference != null
            || element is JccIdentifier && element.namedTokenDef != null

}

class JccStringTokenFindUsagesHandler(val token: Token, private val unit: JccLiteralRegexUnit?)
    : FindUsagesHandler(token.psiElement!!) {


    override fun getPrimaryElements(): Array<PsiElement> = listOfNotNull(token.psiElement, unit).toTypedArray()

}