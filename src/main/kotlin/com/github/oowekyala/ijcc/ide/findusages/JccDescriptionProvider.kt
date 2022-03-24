package com.github.oowekyala.ijcc.ide.findusages

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation

import com.github.oowekyala.ijcc.lang.psi.JccPsiElement

/**
 * Describes elements for the usage view. (TODO)
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JccDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? =
        if (element is JccPsiElement && location is UsageViewLongNameLocation) element.text
        else null
}
