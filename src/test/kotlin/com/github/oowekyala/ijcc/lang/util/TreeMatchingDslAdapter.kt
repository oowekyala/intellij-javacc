package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree
import com.github.oowekyala.treeutils.TreeLikeAdapter
import com.github.oowekyala.treeutils.matchers.MatchingConfig
import com.github.oowekyala.treeutils.matchers.TreeNodeWrapper
import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import com.github.oowekyala.treeutils.printers.KotlintestBeanTreePrinter
import com.intellij.psi.PsiElement
import io.kotlintest.shouldBe


/** An instance of [TreeLikeAdapter] for the [InjectionStructureTree] hierarchy. */
object InjectionTreeHierarchyAdapter : TreeLikeAdapter<InjectionStructureTree> {
    override fun getChildren(node: InjectionStructureTree): List<InjectionStructureTree> = node.children
}
typealias InjectedNodeSpec<N> = TreeNodeWrapper<InjectionStructureTree, N>.() -> Unit

inline fun <reified N : InjectionStructureTree> matchInjectionTree(ignoreChildren: Boolean = false,
                                                                   noinline nodeSpec: InjectedNodeSpec<N>)
        : AssertionMatcher<InjectionStructureTree?> =
        {
            it.baseShouldMatchSubtree(
                MatchingConfig(
                    adapter = InjectionTreeHierarchyAdapter,
                    errorPrinter = KotlintestBeanTreePrinter(InjectionTreeHierarchyAdapter)
                ),
                ignoreChildren,
                nodeSpec
            )
        }


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
        : AssertionMatcher<PsiElement?> =
        { it.baseShouldMatchSubtree(MatchingConfig(adapter = PsiHierarchyAdapter), ignoreChildren, nodeSpec) }

fun <T : InjectionStructureTree> TreeNodeWrapper<InjectionStructureTree, T>.textLeaf(text: String) =
        child<InjectionStructureTree.SurroundNode> {
            it.prefix shouldBe text
            it.suffix shouldBe ""

            it.child shouldBe child<InjectionStructureTree.EmptyLeaf> {}
        }

