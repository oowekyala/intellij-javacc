package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.grammarNature
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JjtreeQNameStubIndex
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JjtNodeToGrammarLineMarkerProvider : BaseTargetingLineMarkerProvider<PsiClass>(PsiClass::class.java) {
    override fun processElt(elt: PsiClass): Sequence<RelatedItemLineMarkerInfo<PsiElement>> =
        elt.qualifiedName
            ?.let { qname ->
                JjtreeQNameStubIndex.get(qname, elt.project, GlobalSearchScope.allScope(elt.project))
            }
            ?.takeIf { it.isNotEmpty() }
            ?.groupBy { it.containingFile }
            ?.mapValues { (grammar, nodes) ->

                if (grammar.grammarNature < GrammarNature.JJTREE) null
                else {
                    elt.nameIdentifier?.let { ident ->
                        NavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_JJTREE_NODE)
                            .setTargets(nodes)
                            .setCellRenderer(JjtPartialDeclCellRenderer)
                            .setTooltipText("Navigate to JJTree node in ${grammar.name}")
                            .setPopupTitle("Select partial declaration in ${grammar.name}")
                            .createLineMarkerInfo(ident)
                    }
                }

            }
            ?.values
            ?.asSequence()
            ?.filterNotNull()
            .orEmpty()


}