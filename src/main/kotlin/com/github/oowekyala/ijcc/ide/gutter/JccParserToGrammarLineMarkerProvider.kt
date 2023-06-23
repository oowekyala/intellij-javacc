package com.github.oowekyala.ijcc.ide.gutter

import com.github.oowekyala.ijcc.icons.JccIcons
import com.github.oowekyala.ijcc.lang.psi.getProductionByName
import com.github.oowekyala.ijcc.lang.psi.grammarForParserClass
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiUtil

/**
 * @author Clément Fournier
 * @since 1.2
 */
class JccParserToGrammarLineMarkerProvider : BaseTargetingLineMarkerProvider<PsiMethod>(PsiMethod::class.java) {

   private val jj3rRegex = Regex("jj_3R_(\\w+)_(\\d+)_(\\d+)_\\d+")

    override fun processElt(elt: PsiMethod): Sequence<RelatedItemLineMarkerInfo<PsiElement>> {
        val jccFile = elt.containingClass?.grammarForParserClass ?: return emptySequence()

        val jj3r = jj3rRegex.matchEntire(elt.name)

        if (jj3r != null) {
            val (_, line, col) = jj3r.destructured

            return elt.nameIdentifier?.let { ident ->
                NavigationGutterIconBuilder.create(JccIcons.GUTTER_NAVIGATE_TO_PRODUCTION)
                    .setTargets(NotNullLazyValue.createValue<Collection<PsiElement>> {

                        val i = line.toInt() - 1 // javacc counts lines from 1, IJ from 0
                        if (i < 0) return@createValue emptyList()

                        // note: very expensive
                        val offset = StringUtil.lineColToOffset(jccFile.text, i, col.toInt())

                        if (offset < 0) return@createValue emptyList()

                        val target = PsiUtil.getElementAtOffset(jccFile, offset)

                        listOfNotNull(target)
                    })

                    .setTooltipText("Navigate to expansion in ${jccFile.name}")
                    .setPopupTitle("JavaCC grammar ${jccFile.name}")
                    .createLineMarkerInfo(ident)
            }?.let { sequenceOf(it) }
                .orEmpty()

        }

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
