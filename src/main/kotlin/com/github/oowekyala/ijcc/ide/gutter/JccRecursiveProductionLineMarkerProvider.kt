package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalExpansionUnit
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
import com.github.oowekyala.ijcc.lang.psi.firstAncestorOrNull
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.util.FunctionUtil
import javax.swing.Icon

/**
 * Highlights recursive production calls.
 * TODO maybe highlight right recursion differently.
 *
 * @author Clément Fournier
 * @since 1.1
 */
object JccRecursiveProductionLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {
    override fun getName(): String? = null

    override fun getIcon(): Icon = JccIcons.GUTTER_RECURSION

    override fun getLineMarkerInfo(elt: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: List<PsiElement>,
                                        result: MutableCollection<LineMarkerInfo<PsiElement>>) {

        for (elt in elements) {
            ProgressManager.checkCanceled()
            (elt as? JccNonTerminalExpansionUnit)
                ?.takeIf { it.isRecursiveCall() }
                ?.let(::JccRecursiveProductionLineMarkerInfo)
                ?.runIt { result += it }
        }
    }

    private class JccRecursiveProductionLineMarkerInfo(element: JccNonTerminalExpansionUnit) :
        LineMarkerInfo<PsiElement>(
            // it needs a leaf element otherwise it complains with assertion errors
            element.nameIdentifier.leaf,
            element.nameIdentifier.textRange,
            JccIcons.GUTTER_RECURSION,
            FunctionUtil.constant("Recursive call"),
            null,
            GutterIconRenderer.Alignment.RIGHT
        )
}

fun JccNonTerminalExpansionUnit.isRecursiveCall(): Boolean =
    name!! == firstAncestorOrNull<JccNonTerminalProduction>()?.name
