package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.stubs.indices.JccParserQnameIndexer
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.injection.InjectedLanguageManager
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

    override fun processElt(elt: PsiClass): Sequence<RelatedItemLineMarkerInfo<PsiElement>> =
        elt.takeUnless { InjectedLanguageManager.getInstance(elt.project).isInjectedFragment(elt.containingFile) }
            ?.qualifiedName
            ?.let { qname ->
                var f: VirtualFile? = null
                FileBasedIndex.getInstance().getFilesWithKey(
                    JccParserQnameIndexer.NAME, setOf(qname), {
                        f = it
                        true
                    },
                    GlobalSearchScope.allScope(elt.project)
                )
                f
            }
            ?.let { vf ->
                PsiManager.getInstance(elt.project).findFile(vf)  as? JccFile
            }
            // filter out the injected compilation unit in PARSER_BEGIN
            ?.takeUnless { it == InjectedLanguageManager.getInstance(elt.project).getTopLevelFile(elt) }
            ?.let { jccFile ->
                elt.nameIdentifier?.let { ident ->
                    NavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_GRAMMAR).setTarget(jccFile)
                        .setTooltipText("Navigate to grammar file ${jccFile.name}")
                        .setPopupTitle("JavaCC grammar ${jccFile.name}")
                        .createLineMarkerInfo(ident)
                }
            }
            ?.let { sequenceOf(it) }
            .orEmpty()

}