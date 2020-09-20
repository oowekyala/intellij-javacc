package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.nodeClass
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement

/**
 * Adds a gutter icon linking a production to a JJTree node class.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JjtreeNodeClassLineMarkerProvider :
    BaseTargetingLineMarkerProvider<JjtNodeClassOwner>(JjtNodeClassOwner::class.java) {

    override fun processElt(elt: JjtNodeClassOwner): Sequence<RelatedItemLineMarkerInfo<PsiElement>> {
        val psiClass = elt.nodeClass ?: return emptySequence()

        val builder = NavigationGutterIconBuilder.create(JccIcons.GUTTER_NODE_CLASS).setTarget(psiClass)
            .setTooltipText("Navigate to class ${psiClass.name}")
            .setPopupTitle("Class ${psiClass.name}")

        val markerBearer = when (elt) {
            is JccNonTerminalProduction -> elt.nameIdentifier
            is JccScopedExpansionUnit   -> elt.jjtreeNodeDescriptor.nameIdentifier
            else                        -> null
        }?.leaf

        return markerBearer?.let { builder.createLineMarkerInfo(it) }?.let { sequenceOf(it) } ?: emptySequence()
    }

}
