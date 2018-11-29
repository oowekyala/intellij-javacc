package com.github.oowekyala.ijcc.reference

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation

/**
 * Describes elements for the usage view. (TODO)
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? =
            when (location) {
                is UsageViewLongNameLocation -> element.text
                else                         -> null
            }
}