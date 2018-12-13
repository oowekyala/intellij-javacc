package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.util.foreachAndBetween

/**
 * Does a prefix traversal on an [InjectionStructureTree] and merges
 * string contexts around [InjectionStructureTree.HostLeaf] into
 * [HostSpec]s.
 */
class TreeLineariserVisitor : InjectionStructureTree.Companion.PrefixVisitor() {

    private val prefixBuilder = StringBuilder()
    private val lastPrefixBuilder = StringBuilder()
    private var lastVisitedHost: HostLeaf? = null

    private val hostSpecs: MutableList<HostSpec> = mutableListOf()

    fun startOn(root: InjectionStructureTree): LinearInjectedStructure {
        lastVisitedHost = root.getLastHostInPrefixOrder() ?: return LinearInjectedStructure(emptyList())

        root.accept(this)

        hostSpecs += lastVisitedHost!!.toSpec(
            lastPrefixBuilder.toString(),
            prefixBuilder.toString()
        )

        return LinearInjectedStructure(hostSpecs.toList())
    }


    override fun visit(hostLeaf: HostLeaf) {
        if (hostLeaf === lastVisitedHost) {
            // taken care of in the root
            lastPrefixBuilder.append(prefixBuilder)
            prefixBuilder.clear()
            return
        }

        val prefix = prefixBuilder.toString()
        prefixBuilder.clear()
        hostSpecs += hostLeaf.toSpec(prefix, null)
    }

    override fun visit(surroundNode: SurroundNode) {
        prefixBuilder.append(surroundNode.prefix)
        surroundNode.child.accept(this)
        prefixBuilder.append(surroundNode.suffix)
    }

    override fun visit(emptyLeaf: EmptyLeaf) {
        // do nothing
    }

    override fun visit(multiChildNode: MultiChildNode) {
        multiChildNode.children.foreachAndBetween({ prefixBuilder.append(multiChildNode.delimiter()) }) {
            it.accept(this)
        }
    }
}