package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.getProductionByName
import com.github.oowekyala.ijcc.lang.psi.grammarForParserClass
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JccParserQnameIndexer
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

/**
 * @author Clément Fournier
 * @since 1.3
 */
object JccProductionToParserLineMarkerProvider : BaseTargetingLineMarkerProvider<PsiMethod>(PsiMethod::class.java) {

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
