package com.github.oowekyala.ijcc.lang.util

import com.intellij.psi.PsiElement

/** An instance of [TreeLikeAdapter] for the [PsiElement] hierarchy. */
object PsiHierarchyAdapter : TreeLikeAdapter<PsiElement> {
    override fun getChildren(node: PsiElement): List<PsiElement> = node.children.toList()

    override fun nodeName(type: Class<out PsiElement>): String =
            type.simpleName.removeSurrounding("Jcc", "Impl")

}

inline fun <reified N : PsiElement> matchNode(ignoreChildren: Boolean = false,
                                              noinline nodeSpec: NWrapper<PsiElement, N>.() -> Unit) =
        baseMatchSubtree(PsiHierarchyAdapter, ignoreChildren, nodeSpec)