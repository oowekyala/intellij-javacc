package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.parserMethod
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement

/**
 * @author Clément Fournier
 * @since 1.3
 */
object JccProductionToParserLineMarkerProvider
    : BaseTargetingLineMarkerProvider<JccNonTerminalProduction>(JccNonTerminalProduction::class.java) {

    override fun processElt(elt: JccNonTerminalProduction): Sequence<RelatedItemLineMarkerInfo<PsiElement>> {

        val target = elt.parserMethod ?: return emptySequence()

        return NavigationGutterIconBuilder.create(JccIcons.GUTTER_PARSER_METHOD)
            .setTarget(target)
            .setTooltipText("Navigate to parser method in ${target.containingClass?.name}")
            .setPopupTitle("Parser class ${target.containingClass?.name}")
            .createLineMarkerInfo(elt.nameIdentifier.leaf)
            .let { sequenceOf(it) }
    }

}
