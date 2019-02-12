package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JccParserQnameIndexer
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccParserToGrammarLineMarkerProvider : BaseTargetingLineMarkerProvider<PsiClass>(PsiClass::class.java) {

    override fun processElt(elt: PsiClass): Sequence<RelatedItemLineMarkerInfo<PsiElement>> {

        val qnames = listOfNotNull(elt.qualifiedName).toSet().takeIf { it.isNotEmpty() } ?: return emptySequence()

        val file: VirtualFile = let {
            var f: VirtualFile? = null
            FileBasedIndex.getInstance().getFilesWithKey(
                JccParserQnameIndexer.NAME, qnames, {
                    f = it
                    true
                },
                GlobalSearchScope.allScope(elt.project)
            )
            f
        } ?: return emptySequence()

        val jccFile = PsiManager.getInstance(elt.project).findFile(file)  as? JccFile ?: return emptySequence()


        val builder =
            NavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_GRAMMAR).setTarget(jccFile)
                .setTooltipText("Navigate to grammar file ${jccFile.name}")
                .setPopupTitle("JavaCC grammar ${jccFile.name}")


        return elt.nameIdentifier?.let { builder.createLineMarkerInfo(it) }?.let { sequenceOf(it) }.orEmpty()
    }

}