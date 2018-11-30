package com.github.oowekyala.ijcc.insight.jjtree

import com.github.oowekyala.ijcc.lang.psi.JccJjtreeNodeDescriptor
import com.github.oowekyala.ijcc.lang.psi.JccNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.util.JavaccIcons
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import gnu.trove.THashSet

/**
 * Adds a gutter icon for JJTree node class.
 *
 * @author Clément Fournier
 * @since 1.0
 */
class JjtreeLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(elements: List<PsiElement>,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
                                          forNavigation: Boolean) {
        val visited = if (forNavigation) THashSet<PsiElement>() else null
        for (element in elements) {
            val elt = element as? JccNodeClassOwner ?: continue
            val isNodeId = (elt is JccNonTerminalProduction || elt is JccJjtreeNodeDescriptor && !elt.isVoid)
            if (!(isNodeId)) continue

            if (forNavigation && !visited!!.add(elt)) continue


            val psiClass = getNodePsiClass(elt) ?: continue


            val action = ActionManager.getInstance().getAction("GotoRelated")
            var tooltipAd = ""
            var popupTitleAd = ""
            if (action != null) {
                val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(action)
                val actionText =
                        if (StringUtil.isEmpty(shortcutText)) "'" + action.templatePresentation.text + "' action" else shortcutText
                tooltipAd = "\nGo to sub-expression code via $actionText"
                popupTitleAd = " (for sub-expressions use $actionText)"
            }
            val title = "Node class"
            val builder = NavigationGutterIconBuilder.create(JavaccIcons.GUTTER_NODE_CLASS).setTarget(psiClass)
                .setTooltipText("Click to navigate to $title$tooltipAd")
                .setPopupTitle(StringUtil.capitalize(title) + popupTitleAd)
            result.add(builder.createLineMarkerInfo(element))
        }
    }


    private fun getNodePsiClass(element: JccNodeClassOwner): NavigatablePsiElement? =
            element.nodeClass(element.containingFile.javaccConfig)
}