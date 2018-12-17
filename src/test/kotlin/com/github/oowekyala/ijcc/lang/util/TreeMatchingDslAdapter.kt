package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree
import com.github.oowekyala.treematchers.TreeLikeAdapter
import com.github.oowekyala.treematchers.TreeNodeWrapper
import com.github.oowekyala.treematchers.baseShouldMatchSubtree
import com.intellij.psi.PsiElement


/** An instance of [TreeLikeAdapter] for the [InjectionStructureTree] hierarchy. */
object InjectionTreeHierarchyAdapter : TreeLikeAdapter<InjectionStructureTree> {
    override fun getChildren(node: InjectionStructureTree): List<InjectionStructureTree> = node.children
}
typealias InjectedNodeSpec<N> = TreeNodeWrapper<InjectionStructureTree, N>.() -> Unit

inline fun <reified N : InjectionStructureTree> matchInjectionTree(ignoreChildren: Boolean = false,
                                                                   noinline nodeSpec: InjectedNodeSpec<N>)
        : AssertionMatcher<InjectionStructureTree?> =
        { it.baseShouldMatchSubtree(InjectionTreeHierarchyAdapter, ignoreChildren, nodeSpec) }


/** An instance of [TreeLikeAdapter] for the [PsiElement] hierarchy. */
object PsiHierarchyAdapter : TreeLikeAdapter<PsiElement> {
    override fun getChildren(node: PsiElement): List<PsiElement> = node.children.toList()

    override fun nodeName(type: Class<out PsiElement>): String =
            type.simpleName.removePrefix("Jcc").removeSuffix("Impl")

}

typealias PsiSpec<N> = TreeNodeWrapper<PsiElement, N>.() -> Unit
typealias AssertionMatcher<T> = (T) -> Unit


inline fun <reified N : PsiElement> matchPsi(ignoreChildren: Boolean = false,
                                             noinline nodeSpec: PsiSpec<N>)
        : AssertionMatcher<PsiElement?> = { it.baseShouldMatchSubtree(PsiHierarchyAdapter, ignoreChildren, nodeSpec) }



