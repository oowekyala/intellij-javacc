package com.github.oowekyala.ijcc.lang.injection

import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * Tree that mirrors the control flow structure
 * of the generated Java code.
 * The tree is built from a Psi tree with an [InjectedTreeBuilderVisitor].
 * Prefixes and suffixes then need to be merged around hosts to
 * define the injection places for a [MultiHostRegistrar],
 * which is performed by [TreeLineariserVisitor].
 *
 * @see JavaccLanguageInjector
 */
sealed class InjectionStructureTree(open val children: List<InjectionStructureTree> = emptyList()) {

    /** Dispatched the visitor on the runtime type of this tree node. */
    abstract fun accept(visitor: InjectionStructureTreeVisitor)

    /** Gets the last [HostLeaf] that will be visited when performing a prefix traversal on this subtree. */
    fun getLastHostInPrefixOrder(): HostLeaf? {
        return when (this) {
            is EmptyLeaf          -> null
            is HostLeaf           -> this
            is StructuralBoundary -> child.getLastHostInPrefixOrder()
            is SurroundNode       -> child.getLastHostInPrefixOrder()
            is MultiChildNode     -> children.asReversed().asSequence().map { it.getLastHostInPrefixOrder() }.firstOrNull { it != null }
        }
    }

    /**
     * Placeholder used to preserve arity of nodes, even if the contents
     * of a branch is not interesting.
     */
    object EmptyLeaf : InjectionStructureTree() {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)
        override fun toString(): String = "EmptyLeaf"
    }


    /**
     * Specify that the subtree must keep all its suffixes together and not append it
     * to the next siblings. If the subtree doesn't contain any hosts, *the whole subtree is dropped*!
     */
    data class StructuralBoundary(val child: InjectionStructureTree) : InjectionStructureTree(listOf(child)) {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)
    }

    /**
     * Leaf wrapping a [PsiLanguageInjectionHost].
     * The [host] is subject to replacement, which is handled only by [HostSpec],
     * so it should only be used for tests.
     */
    data class HostLeaf(val host: PsiLanguageInjectionHost,
                        private val rangeGetter: (PsiLanguageInjectionHost) -> TextRange)
        : InjectionStructureTree() {

        fun toSpec(prefix: String?, suffix: String?): HostSpec = HostSpec(prefix, suffix, host, rangeGetter)

        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)

        override fun toString(): String = """
            HostLeaf: {
                ${host.text.myIndent()}
            }
        """.trimIndent()
    }

    /**
     * Node adding a string context around another node.
     *
     * @property child The wrapped node
     * @property prefix String to prepend to the [child]
     * @property suffix String to append to the [child]
     */
    data class SurroundNode(val child: InjectionStructureTree,
                            val prefix: String,
                            val suffix: String) : InjectionStructureTree(listOf(child)) {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)

        override fun toString(): String = """
            SurroundNode: {
                prefix: {
                ${prefix.myIndent()}
                }
                suffix: {
                ${suffix.myIndent()}
                },
                child:
                ${child.toString().myIndent()}
            }
        """.trimIndent()
    }

    /**
     * Node having several children, delimited by [delimiter].
     *
     * @property delimiter A supplier for the delimiter. Not a string bc the
     *                      number of delimiters to get is initially not known,
     *                      and we may want to have a different delimiter between
     *                      children
     *
     * @property children Children of this node
     */
    data class MultiChildNode(override val children: List<InjectionStructureTree>,
                              val delimiter: () -> String) : InjectionStructureTree() {
        override fun accept(visitor: InjectionStructureTreeVisitor) = visitor.visit(this)
        override fun toString(): String = """
            MultiChildNode: {
               delimiter: {
               ${delimiter().myIndent()}
               }
               children: [
               ${children.joinToString(separator = ",\n").myIndent()}
               ]
            }
        """.trimIndent()
    }

    companion object {

        // The prependIndent level is to account for the indent of this source file,
        // which is removed by trimIndent in raw strings!
        // the first line still needs to be adjusted otherwise it's over indented
        private fun String.myIndent(): String = prependIndent("     ").lineSequence().mapIndexed { i, s ->
            when (i) {
                0    -> s.replaceIndent("    ")
                else -> s
            }
        }.joinToString(separator = "\n")

        abstract class InjectionStructureTreeVisitor {

            abstract fun defaultVisit(tree: InjectionStructureTree)
            open fun visit(emptyLeaf: InjectionStructureTree.EmptyLeaf) = defaultVisit(emptyLeaf)
            open fun visit(hostLeaf: HostLeaf) = defaultVisit(hostLeaf)
            open fun visit(surroundNode: SurroundNode) = defaultVisit(surroundNode)
            open fun visit(multiChildNode: MultiChildNode) = defaultVisit(multiChildNode)
            open fun visit(structuralBoundary: StructuralBoundary) = defaultVisit(structuralBoundary)
        }

        abstract class PrefixVisitor : InjectionStructureTreeVisitor() {
            override fun defaultVisit(tree: InjectionStructureTree) {
                tree.children.forEach { it.accept(this) }
            }
        }

    }

}
