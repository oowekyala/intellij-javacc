package com.github.oowekyala.ijcc.lang.injection

import com.github.oowekyala.ijcc.lang.injection.InjectionStructureTree.*
import com.github.oowekyala.ijcc.util.foreachAndBetween
import com.github.oowekyala.ijcc.util.removeLast

/**
 * Does a prefix traversal on an [InjectionStructureTree] and merges
 * string contexts around [InjectionStructureTree.HostLeaf] into
 * [HostSpec]s.
 */
class TreeLineariserVisitor private constructor(knownPrefixBuilder: StringBuilder)
    : InjectionStructureTree.Companion.PrefixVisitor() {

    // The known prefix builder transmits prefix context to children visitors when crossing a structural boundary
    // It's shared for all spawned subvisitors, to avoid clearing the prefix in case
    // a structural boundary contains no host
    private val prefixBuilder = knownPrefixBuilder
    private val lastPrefixBuilder = StringBuilder()
    private var lastVisitedHost: HostLeaf? = null

    private var appendEverythingToLastSpec = false

    private val hostSpecs: MutableList<HostSpec> = mutableListOf()

    fun startOn(root: InjectionStructureTree): LinearInjectedStructure {
        lastVisitedHost = root.getLastHostInPrefixOrder() ?: return LinearInjectedStructure(emptyList())

        root.accept(this)

        if (appendEverythingToLastSpec) {
            // we're in the case explained in visit(StructuralBoundary)
            // lastVisitedSpec != null
            hostSpecs += hostSpecs.removeLast().appendSuffixAndDestroy(prefixBuilder)

        } else if (lastVisitedHost != null) {

            hostSpecs += lastVisitedHost!!.toSpec(
                prefix = lastPrefixBuilder.toString(),
                suffix = prefixBuilder.toString()
            )
        }

        prefixBuilder.clear()

        return LinearInjectedStructure(hostSpecs.toList())
    }


    override fun visit(structuralBoundary: StructuralBoundary) {
        // The subtree should keep its last suffix for itself
        // instead of adding it to the prefix of the next host

        val visitor = TreeLineariserVisitor(prefixBuilder)

        hostSpecs += visitor.startOn(structuralBoundary.child).hostSpecs

        // the visitor flushed the prefixBuilder into its last suffix,
        // or didn't encounter any host, so the prefixBuilder will still
        // be used as the prefix of the next host

        if (visitor.lastVisitedHost === this.lastVisitedHost) {
            // The subtree below the boundary is necessarily the last
            // one to be visited by this visitor (we can only go up from here).
            // So within the structural boundary [this] visitor explores,
            // we'll now only append strings to the last suffix
            // -> but we need instead to append them to the last host spec
            // of the subtree, since there's no more node to explore here

            appendEverythingToLastSpec = true // signals the above to startOn
        }
    }


    override fun visit(hostLeaf: HostLeaf) {
        if (hostLeaf === lastVisitedHost) {
            // taken care of in startOn
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

    companion object {

        /** Linearise the given structure tree into a structure suitable for injection. */
        fun linearise(tree: InjectionStructureTree): LinearInjectedStructure =
            TreeLineariserVisitor(StringBuilder()).startOn(tree)

    }
}