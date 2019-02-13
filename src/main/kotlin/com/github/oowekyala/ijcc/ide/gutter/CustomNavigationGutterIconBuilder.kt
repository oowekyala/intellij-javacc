package com.github.oowekyala.ijcc.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.codeInsight.navigation.NavigationGutterIconRenderer
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.util.NotNullFunction
import javax.swing.Icon

/**
 * @author Clément Fournier
 * @since 1.2
 */
class CustomNavigationGutterIconBuilder<T>(icon: Icon,
                                           converter: NotNullFunction<T, Collection<PsiElement>>,
                                           gotoRelatedItemProvider: NotNullFunction<T, Collection<GotoRelatedItem>>?) :
    NavigationGutterIconBuilder<T>(icon, converter, gotoRelatedItemProvider) {


    override fun createLineMarkerInfo(element: PsiElement): RelatedItemLineMarkerInfo<PsiElement> {
        return super.createLineMarkerInfo(element)
    }

    public override fun getGotoTargets(): NotNullLazyValue<Collection<GotoRelatedItem>> =
        super.getGotoTargets()

    @Suppress("INACCESSIBLE_TYPE")
    fun createIconRenderer(project: Project) = super.createGutterIconRenderer(project) as NavigationGutterIconRenderer


    companion object {

        fun create(icon: Icon): CustomNavigationGutterIconBuilder<PsiElement> =
            create(icon, DEFAULT_PSI_CONVERTOR, PSI_GOTO_RELATED_ITEM_PROVIDER)

        fun <T> create(icon: Icon,
                       converter: NotNullFunction<T, Collection<PsiElement>>): CustomNavigationGutterIconBuilder<T> =
            create(icon, converter, null)

        fun <T> create(icon: Icon,
                       converter: NotNullFunction<T, Collection<PsiElement>>,
                       gotoRelatedItemProvider: NotNullFunction<T, Collection<GotoRelatedItem>>?): CustomNavigationGutterIconBuilder<T> =
            CustomNavigationGutterIconBuilder(icon, converter, gotoRelatedItemProvider)
    }
}