package com.github.oowekyala.ijcc.insight.jjtree

import com.github.oowekyala.ijcc.lang.psi.JccNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.JccScopedExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.nodeSimpleName
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement

/**
 * Adds a gutter icon linking a node bearer to its other declarations.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JjtreePartialDeclarationLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(elements: List<PsiElement>,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
                                          forNavigation: Boolean) {
        val partialDeclarations = elements
            .groupBy { (it as? JccNodeClassOwner)?.nodeSimpleName }
            .filterValues { it.size > 1 }

        for ((name, group) in partialDeclarations) {

            if (name == null) continue

            val builder =
                    NavigationGutterIconBuilder.create(JavaccIcons.GUTTER_PARTIAL_DECL)
                        .setTargets(group)
                        .setCellRenderer(PartialDeclCellRenderer())
                        .setTooltipText("Click to navigate to other declarations of $name")
                        .setPopupTitle("Select partial declaration for $name")

            for (elt in group) {

                val markerBearer = when (elt) {
                    is JccNonTerminalProduction -> elt.nameIdentifier
                    is JccScopedExpansionUnit   -> elt.jjtreeNodeDescriptor.nameIdentifier
                    else                        -> null
                }

                markerBearer?.let { builder.createLineMarkerInfo(it) }?.runIt { result.add(it) }
            }
        }
    }


}