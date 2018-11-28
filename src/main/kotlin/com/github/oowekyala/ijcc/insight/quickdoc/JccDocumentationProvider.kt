package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.psi.JccIdentifier
import com.github.oowekyala.ijcc.lang.psi.JccRegularExpressionReference
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

/**
 * Documentation extension point.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JccDocumentationProvider : AbstractDocumentationProvider() {


    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return when {
            element is JccIdentifier && originalElement is JccRegularExpressionReference -> "< ${element.name} >"
            else                                                                         -> null
        }
    }


}