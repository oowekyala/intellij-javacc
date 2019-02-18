package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.*
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement

/**
 * Adds a gutter icon linking a node bearer to its other declarations.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JjtreePartialDeclarationLineMarkerProvider :
    BaseTargetingLineMarkerProvider<JjtNodeClassOwner>(JjtNodeClassOwner::class.java), DumbAware {


    override fun processElt(elt: JjtNodeClassOwner): Sequence<RelatedItemLineMarkerInfo<PsiElement>> =
        elt.typedReference
            ?.lightMultiResolve()
            ?.takeIf { it.size > 1 }
            ?.let { targets ->
                val nodeName = targets[0].nodeSimpleName!!

                NavigationGutterIconBuilder.create(JccIcons.GUTTER_PARTIAL_DECL)
                    .setTargets(targets)
                    .setCellRenderer(JjtPartialDeclCellRenderer)
                    .setTooltipText("Navigate to partial declarations of $nodeName")
                    .setPopupTitle("Select partial declaration for $nodeName")
            }?.let { builder ->
                when (elt) {
                    is JccNonTerminalProduction -> elt.nameIdentifier
                    is JccScopedExpansionUnit   -> elt.nodeIdentifier
                    else                        -> null
                }?.leaf?.let {
                    builder.createLineMarkerInfo(it)
                }
            }
            ?.let { sequenceOf(it) }
            .orEmpty()


}