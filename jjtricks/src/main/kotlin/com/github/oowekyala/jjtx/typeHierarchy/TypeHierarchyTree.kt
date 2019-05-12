package com.github.oowekyala.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.reporting.MessageCategory.*
import com.github.oowekyala.jjtx.reporting.report
import com.github.oowekyala.jjtx.util.*
import com.github.oowekyala.treeutils.TreeLikeAdapter

/**
 * Tree representing a node from the grammar, holding
 * information about its subtypes and supertype.
 *
 * The construction process of the tree is iterative,
 * and creates a new tree at each step. Those intermediary
 * representations are not made visible to a consumer of the API.
 * The documentation of this class assumes the finished
 * form of the tree.
 *
 * @property nodeName The fully qualified name of the represented node
 * @property positionInfo The position in the jjtopts file where this node was defined, or null
 * @property children The children (subtypes) of this node
 * @property parent The direct supertype of this node, or null if this is the root
 *
 * @author Clément Fournier
 */
internal class TypeHierarchyTree internal constructor(
    val nodeName: String,
    val positionInfo: Position?,
    children: List<TypeHierarchyTree>,
    internal val specificity: Specificity = Specificity.UNKNOWN
) : TreeOps<TypeHierarchyTree> {


    override val adapter: TreeLikeAdapter<TypeHierarchyTree> = TreeLikeWitness

    var parent: TypeHierarchyTree? = null
        private set

    private val realChildren: MutableList<TypeHierarchyTree> = children.toMutableList()


    val children: List<TypeHierarchyTree>
        get() = realChildren

    private var processed = false

    internal fun removeFromParent() {
        this.parent?.realChildren?.remove(this)
        this.parent = null
    }

    init {
        children.forEach { it.parent = this }
    }

    fun deepCopy(): TypeHierarchyTree {
        return TypeHierarchyTree(
            nodeName,
            positionInfo,
            children.map { it.deepCopy() },
            specificity
        )
    }

    internal fun copy(nodeName: String = this.nodeName,
                      positionInfo: Position? = this.positionInfo,
                      children: List<TypeHierarchyTree> = this.children,
                      specificity: Specificity = this.specificity): TypeHierarchyTree =
        TypeHierarchyTree(
            nodeName, positionInfo, children, specificity
        )

    fun process(ctx: JjtxContext): TypeHierarchyTree {
        if (processed) throw IllegalStateException("Node already processed")
        val jjtreeDeclsByRawName = ctx.grammarFile.allJjtreeDecls
        val expanded = this.expandAllNames(jjtreeDeclsByRawName.keys, ctx)
        val dedup = expanded.removeDuplicates(ctx)
        val adopted = dedup.adoptOrphansOnRoot(jjtreeDeclsByRawName.values.flatten(), ctx)
        adopted.descendantsOrSelf().forEach { it.processed = true }
        return adopted
    }

    internal companion object {

        fun default() =
            default(JjtxOptsModel.DefaultRootNodeName)

        fun default(rootName: String) =
            TypeHierarchyTree(rootName,
                JsonPosition("jjtx.typeHierarchy"), emptyList(), specificity = Specificity.ROOT)

        /**
         * First construction pass, from a Json object.
         */
        fun fromData(json: DataAstNode?, ctx: JjtxContext): TypeHierarchyTree = when (json) {
            null -> default()
            else ->
                json.toTree(ctx)?.copy(specificity = Specificity.ROOT) ?: default()
        }


        private fun DataAstNode.toTree(ctx: JjtxContext): TypeHierarchyTree? {


            return when (this) {
                is AstMap    -> this.fromJsonObject(ctx)
                is AstScalar -> this.fromJsonPrimitive(ctx)
                else         -> {
                    ctx.messageCollector.report(
                        "expected string or object, got $this",
                        WRONG_TYPE,
                        position
                    )
                    null
                }
            }
        }

        private fun AstScalar.fromJsonPrimitive(ctx: JjtxContext): TypeHierarchyTree? {
            return if (type == ScalarType.STRING) {
                TypeHierarchyTree(any, position, emptyList())
            } else {
                ctx.messageCollector.report("expected string, got ${this}", WRONG_TYPE, position)
                null
            }
        }

        private fun AstMap.fromJsonObject(ctx: JjtxContext): TypeHierarchyTree? {

            if (size > 1) {
                ctx.messageCollector.report("$size", MULTIPLE_HIERARCHY_ROOTS, position)
                return null
            } else if (size == 0) {
                ctx.messageCollector.report("", NO_HIERARCHY_ROOTS, position)
                return null
            }

            val name = keys.first()

            return when (val children = this[name]) {
                is AstScalar -> TypeHierarchyTree(
                    nodeName = name,
                    children = listOfNotNull(children.fromJsonPrimitive(ctx)),
                    positionInfo = position
                )
                is AstSeq    -> TypeHierarchyTree(
                    nodeName = name,
                    children = children.mapNotNull { it.toTree(ctx) },
                    positionInfo = position
                )
                else         -> {
                    ctx.messageCollector.report(
                        "expected array or string, got $children",
                        WRONG_TYPE,
                        position
                    )
                    null
                }
            }
        }
    }
}

private object TreeLikeWitness : TreeLikeAdapter<TypeHierarchyTree> {
    override fun getChildren(node: TypeHierarchyTree): List<TypeHierarchyTree> = node.children

    override fun nodeName(node: TypeHierarchyTree): String = node.nodeName
}
