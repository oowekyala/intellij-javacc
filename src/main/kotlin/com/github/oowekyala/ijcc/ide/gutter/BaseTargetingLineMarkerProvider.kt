package com.github.oowekyala.ijcc.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.psi.PsiElement
import gnu.trove.THashSet

/**
 * @author Clément Fournier
 * @since 1.2
 */
abstract class BaseTargetingLineMarkerProvider<in T : PsiElement>(private val target: Class<T>)
    : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(elements: MutableList<PsiElement>,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>,
                                          forNavigation: Boolean) {
        // prunes duplicates when collecting for nav
        val visited = THashSet<PsiElement>()

        elements.asSequence()
            .filter { !forNavigation || visited.add(it) }
            .filter { target.isInstance(it) }
            .map { target.cast(it) }
            .flatMap { processElt(it) }
            .forEach { result.add(it) }
    }

    abstract fun processElt(elt: T): Sequence<RelatedItemLineMarkerInfo<PsiElement>>

}