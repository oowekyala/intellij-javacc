package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JavaccIcons
import com.github.oowekyala.ijcc.lang.index.JccParserQnameIndexer
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.util.runIt
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import gnu.trove.THashSet

/**
 * @author Clément Fournier
 * @since 1.2
 */
object JccParserToGrammarLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(elements: List<PsiElement>,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
                                          forNavigation: Boolean) {
        // prunes duplicates when collecting for nav
        val visited = if (forNavigation) THashSet<PsiElement>() else null
        for (element in elements) {
            val elt = element as? PsiClass ?: continue
            if (forNavigation && !visited!!.add(elt)) continue

            val file: VirtualFile = let {
                var f: VirtualFile? = null
                FileBasedIndex.getInstance().getFilesWithKey(
                    JccParserQnameIndexer.NAME, setOf(elt.qualifiedName), {
                        f = it
                        true
                    },
                    GlobalSearchScope.allScope(elt.project)
                )
                f
            } ?: return

            val jccFile = PsiManager.getInstance(elt.project).findFile(file)  as? JccFile ?: return


            val builder =
                NavigationGutterIconBuilder.create(JavaccIcons.GUTTER_NAVIGATE_TO_GRAMMAR).setTarget(jccFile)
                    .setTooltipText("Navigate to grammar file ${jccFile.name}")
                    .setPopupTitle("JavaCC grammar ${jccFile.name}")


            elt.nameIdentifier?.let { builder.createLineMarkerInfo(it) }?.runIt { result.add(it) }
        }
    }
}