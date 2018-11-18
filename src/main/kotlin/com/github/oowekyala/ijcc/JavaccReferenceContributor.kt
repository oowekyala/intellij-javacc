package com.github.oowekyala.ijcc

/**
 * @author Clément Fournier
 * @since 1.0
 */

import com.github.oowekyala.ijcc.lang.reference.JccProductionReference
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

object SimpleReferenceContributor : PsiReferenceContributor() {
// prob not needed
    @Override
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(

            PlatformPatterns.psiElement(PsiLiteralExpression::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val literalExpression = element as PsiLiteralExpression
                    val value = literalExpression.value as? String
                    if (value != null && value.startsWith("simple" + ":")) { // TODO
                        return arrayOf(
                            JccProductionReference(
                                element,
                                TextRange(8, value.length + 1)
                            )
                        )
                    }
                    return PsiReference.EMPTY_ARRAY;
                }
            })
    }
}
