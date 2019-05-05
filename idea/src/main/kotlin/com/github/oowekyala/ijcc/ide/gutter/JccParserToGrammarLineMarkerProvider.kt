package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.getProductionByName
import com.github.oowekyala.ijcc.lang.psi.grammarForParserClass
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccParserToGrammarLineMarkerProvider : BaseTargetingLineMarkerProvider<PsiMethod>(PsiMethod::class.java) {

    override fun processElt(elt: PsiMethod): Sequence<RelatedItemLineMarkerInfo<PsiElement>> {
        val jccFile = elt.containingClass?.grammarForParserClass ?: return emptySequence()


        return jccFile
            .getProductionByName(elt.name)
            ?.let { prod ->
                elt.nameIdentifier?.let { ident ->
                    NavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_PRODUCTION)
                        .setTarget(prod)
                        .setTooltipText("Navigate to ${prod.name} in ${jccFile.name}")
                        .setPopupTitle("JavaCC grammar ${jccFile.name}")
                        .createLineMarkerInfo(ident)
                }
            }
            ?.let { sequenceOf(it) }
            .orEmpty()
    }

}
