package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.ijcc.lang.psi.grammarSearchScope
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JjtreeQNameStubIndex
import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiFunctionalExpression
import com.intellij.psi.util.PsiExpressionTrimRenderer
import com.intellij.util.ConstantFunction
import com.intellij.util.Function
import javax.swing.Icon

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JjtNodeToGrammarLineMarkerProvider : BaseTargetingLineMarkerProvider<PsiClass>(PsiClass::class.java) {
    override fun processElt(elt: PsiClass): Sequence<NodeNavigationInfo> =
        elt.qualifiedName
            ?.let { qname ->
                JjtreeQNameStubIndex.get(qname, elt.project, elt.containingFile.grammarSearchScope)
            }
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                it.groupBy { it.containingFile }
            }
            ?.mapValues { (grammar, decls) ->
                elt.nameIdentifier?.let {
                    NodeNavigationInfo(it, grammar, decls)
                }
            }
            ?.values
            ?.asSequence()
            ?.filterNotNull()
            .orEmpty()


    class NodeNavigationInfo(annotationBearer: PsiElement,
                             grammarFile: JccFile,
                             targets: List<JjtNodeClassOwner>)
        : RelatedItemLineMarkerInfo<PsiElement>(
        annotationBearer,
        annotationBearer.textRange,
        JccIcons.GUTTER_NAVIGATE_TO_JJTREE_NODE,
        Pass.LINE_MARKERS,
        ConstantFunction("Navigate to JJTree node in ${grammarFile.name}"),
        createNavigator(annotationBearer.project, targets),
        GutterIconRenderer.Alignment.LEFT,
        getGotoTargets(targets)
    ) {

        override fun canMergeWith(info: MergeableLineMarkerInfo<*>): Boolean {
            if (info !is NodeNavigationInfo) return false
            val otherElement = info.getElement()
            val myElement = element
            return otherElement != null && myElement != null
        }


        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>>): Icon = myIcon

        override fun getCommonTooltip(infos: List<MergeableLineMarkerInfo<*>>): Function<in PsiElement, String> =
            Function { "Multiple grammar files found" }

        override fun getElementPresentation(element: PsiElement): String {
            val parent = element.parent
            return if (parent is PsiFunctionalExpression) {
                PsiExpressionTrimRenderer.render(parent as PsiExpression)
            } else super.getElementPresentation(element)
        }

        companion object {

            fun createNavigator(project: Project,
                                targets: List<JjtNodeClassOwner>): GutterIconNavigationHandler<PsiElement>? {
                val builder = CustomNavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_JJTREE_NODE)
                    .setTargets(targets)
                    .setCellRenderer(JjtPartialDeclCellRenderer)

                return (builder as CustomNavigationGutterIconBuilder).createIconRenderer(project).takeIf { it.isNavigateAction }
            }

            fun getGotoTargets(targets: List<JjtNodeClassOwner>): NotNullLazyValue<Collection<GotoRelatedItem>> {
                val builder = CustomNavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_JJTREE_NODE)
                    .setTargets(targets)
                    .setCellRenderer(JjtPartialDeclCellRenderer)

                return (builder as CustomNavigationGutterIconBuilder).gotoTargets
            }

        }
    }


}