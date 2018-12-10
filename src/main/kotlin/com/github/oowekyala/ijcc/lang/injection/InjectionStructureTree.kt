package com.github.oowekyala.ijcc.lang.injection

import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * Tree that mirrors the control flow structure
 * of the generated Java code. The tree then needs
 * to merge prefixes and suffixes around hosts to
 * define the injection places for a [MultiHostRegistrar]
 */
sealed class InjectionStructureTree(open val children: List<InjectionStructureTree> = emptyList()) {


    abstract fun accept(visitor: InjectionStructureTreeVisitor)

    fun flattenWithPrefixOrdering(): Sequence<InjectionStructureTree> {
        return sequenceOf(this) + children.asSequence().flatMap { it.flattenWithPrefixOrdering() }
    }

    //
    fun getLastHostInPrefixOrder(): HostLeaf? {
        return when (this) {
            is EmptyLeaf      -> null
            is HostLeaf       -> this
            is SurroundNode   -> child.getLastHostInPrefixOrder()
            is MultiChildNode -> children.asReversed().asSequence().map { it.getLastHostInPrefixOrder() }.firstOrNull { it != null }
        }
    }

    object EmptyLeaf : InjectionStructureTree() {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)
    }

    data class HostLeaf(val host: PsiLanguageInjectionHost) : InjectionStructureTree() {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)
    }

    data class SurroundNode(val child: InjectionStructureTree,
                            val prefix: String,
                            val suffix: String) : InjectionStructureTree(listOf(child)) {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)
    }

    data class MultiChildNode(override val children: List<InjectionStructureTree>,
                              val delimiter: () -> String) : InjectionStructureTree() {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)
    }

    companion object {


        abstract class InjectionStructureTreeVisitor {

            abstract fun defaultVisit(tree: InjectionStructureTree)
            open fun visit(emptyLeaf: EmptyLeaf) = defaultVisit(emptyLeaf)
            open fun visit(hostLeaf: HostLeaf) = defaultVisit(hostLeaf)
            open fun visit(surroundNode: SurroundNode) = defaultVisit(surroundNode)
            open fun visit(multiChildNode: MultiChildNode) = defaultVisit(multiChildNode)
        }

        abstract class PrefixVisitor : InjectionStructureTreeVisitor() {
            override fun defaultVisit(tree: InjectionStructureTree) {
                tree.children.forEach { it.accept(this) }
            }
        }

    }

}