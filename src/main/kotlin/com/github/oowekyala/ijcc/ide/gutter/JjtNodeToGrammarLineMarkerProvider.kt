package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JccParserQnameIndexer
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.stubs.JccFileStub
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JjtreeQNameStubIndex
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
object JjtNodeToGrammarLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(elements: List<PsiElement>,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
                                          forNavigation: Boolean) {
        // prunes duplicates when collecting for nav
        val visited = if (forNavigation) THashSet<PsiElement>() else null
        for (element in elements) {
            val elt = element as? PsiClass ?: continue
            if (forNavigation && !visited!!.add(elt)) continue

            val qname = elt.qualifiedName ?: return

            val jjtreeNodes = JjtreeQNameStubIndex.get(qname, elt.project, GlobalSearchScope.allScope(elt.project))


            if (jjtreeNodes.isEmpty()) return

            val builder =
                NavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_JJTREE_NODE)
                    .setTargets(jjtreeNodes)
                    .setTooltipText("Navigate to JJTree production in ${jjtreeNodes.first().stub.getParentStubOfType(JccFile::class.java)!!.name}")
                    .setPopupTitle("JJTree node navigation")


            elt.nameIdentifier?.let { builder.createLineMarkerInfo(it) }?.runIt { result.add(it) }
        }
    }
}