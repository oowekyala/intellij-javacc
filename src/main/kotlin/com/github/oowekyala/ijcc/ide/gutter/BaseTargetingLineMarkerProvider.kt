package com.github.oowekyala.ijcc.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.psi.PsiElement

/**
 * @author Clément Fournier
 * @since 1.2
 */
abstract class BaseTargetingLineMarkerProvider<in T : PsiElement>(private val target: Class<T>)
    : RelatedItemLineMarkerProvider() {


    override fun collectNavigationMarkers(element: PsiElement,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {

        element.takeIf { target.isInstance(it) }
            ?.let { target.cast(it) }
            ?.let { processElt(it) }
            ?.forEach { result.add(it) }
    }

    abstract fun processElt(elt: T): Sequence<RelatedItemLineMarkerInfo<PsiElement>>

}