package com.github.oowekyala.ijcc.lang.util

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree

/** An instance of [TreeLikeAdapter] for the [InjectionStructureTree] hierarchy. */
object InjectionTreeHierarchyAdapter : TreeLikeAdapter<InjectionStructureTree> {

    override fun getChildren(node: InjectionStructureTree): List<InjectionStructureTree> = node.children

    override fun nodeName(type: Class<out InjectionStructureTree>): String = type.simpleName

}


inline fun <reified N : InjectionStructureTree> matchInjectionTree(ignoreChildren: Boolean = false,
                                                                   noinline nodeSpec: NWrapper<InjectionStructureTree, N>.() -> Unit) =
        baseMatchSubtree(InjectionTreeHierarchyAdapter, ignoreChildren, nodeSpec)