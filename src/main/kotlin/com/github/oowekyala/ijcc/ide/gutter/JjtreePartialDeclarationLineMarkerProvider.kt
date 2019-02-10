package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.lang.psi.*
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement

/**
 * Adds a gutter icon linking a node bearer to its other declarations.
 *
 * @author Clément Fournier
 * @since 1.0
 */
object JjtreePartialDeclarationLineMarkerProvider : RelatedItemLineMarkerProvider(), DumbAware {

    override fun collectNavigationMarkers(elements: List<PsiElement>,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
                                          forNavigation: Boolean) {
        val partialDeclarations = elements
            .mapNotNull { (it as? JccNodeClassOwner)?.typedReference?.multiResolve(false) }
            .filter { it.size > 1 }
            .map { it.map { it.element }.toList() }
            .associateBy { it[0].nodeSimpleName }

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