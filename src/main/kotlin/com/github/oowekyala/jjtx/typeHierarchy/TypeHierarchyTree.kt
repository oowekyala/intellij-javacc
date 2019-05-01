package com.github.oowekyala.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls
import com.github.oowekyala.jjtx.ErrorCollector.Category.*
import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.JjtxOptsModel
import com.github.oowekyala.jjtx.JsonPosition
import com.github.oowekyala.jjtx.Position
import com.github.oowekyala.treeutils.TreeLikeAdapter
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * @author Clément Fournier
 */
class TypeHierarchyTree(
    val nodeName: String,
    val positionInfo: Position,
    children: List<TypeHierarchyTree>,
    val specificity: Specificity = Specificity.UNKNOWN,
    val external: Boolean = false
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

    fun copy(nodeName: String = this.nodeName,
             positionInfo: Position = this.positionInfo,
             children: List<TypeHierarchyTree> = this.children,
             specificity: Specificity = this.specificity,
             external: Boolean = this.external): TypeHierarchyTree =
        TypeHierarchyTree(
            nodeName, positionInfo, children, specificity, external
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

    companion object {

        fun default() =
            default(JjtxOptsModel.DefaultRootNodeName)

        fun default(rootName: String) =
            TypeHierarchyTree(rootName, JsonPosition("jjtx.typeHierarchy"), emptyList(), specificity = Specificity.ROOT)

        /**
         * First construction pass, from a Json object.
         */
        fun fromJson(json: JsonElement?, ctx: JjtxContext): TypeHierarchyTree = when (json) {
            null -> default()
            else ->
                json.toTree(JsonPosition("jjtx.typeHierarchy"), ctx)
                    ?.copy(specificity = Specificity.ROOT)
                    ?: default()
        }


        private fun JsonElement.toTree(parentPosition: JsonPosition,
                                       ctx: JjtxContext): TypeHierarchyTree? {


            return when (this) {
                is JsonObject    -> this.fromJsonObject(parentPosition, ctx)
                is JsonPrimitive -> this.fromJsonPrimitive(parentPosition, ctx)
                else             -> {
                    ctx.errorCollector.handleError(
                        "expected string or object, got $this",
                        WRONG_TYPE,
                        null,
                        parentPosition.resolve(this.toString())
                    )
                    null
                }
            }
        }

        private fun JsonPrimitive.fromJsonPrimitive(parentPosition: JsonPosition,
                                                    ctx: JjtxContext): TypeHierarchyTree? {

            val myPosition = parentPosition.resolve(this.toString())
            return if (isString) {
                TypeHierarchyTree(asString, myPosition, emptyList())
            } else {
                ctx.errorCollector.handleError("expected string, got ${this}", WRONG_TYPE, null, myPosition)
                null
            }
        }

        private fun JsonObject.fromJsonObject(parentPosition: JsonPosition,
                                              ctx: JjtxContext): TypeHierarchyTree? {

            if (size() > 1) {
                ctx.errorCollector.handleError("${size()}", MULTIPLE_HIERARCHY_ROOTS, null, parentPosition)
                return null
            } else if (size() == 0) {
                ctx.errorCollector.handleError("", NO_HIERARCHY_ROOTS, null, parentPosition)
                return null
            }

            val name = keySet().first()
            val position = parentPosition.resolve(name)

            val children = this[name]

            if (children is JsonPrimitive) {

            } else if (children !is JsonArray) {

            }

            return when (children) {
                is JsonPrimitive -> TypeHierarchyTree(nodeName = name, children = listOfNotNull(children.fromJsonPrimitive(position, ctx)), positionInfo = position)
                is JsonArray -> TypeHierarchyTree(
                    nodeName = name,
                    children = children.mapNotNull { it.toTree(position, ctx) },
                    positionInfo = position
                )
                else -> {
                    ctx.errorCollector.handleError(
                        "expected array or string, got $children",
                        WRONG_TYPE,
                        null,
                        position
                    )
                    null
                }
            }
        }
    }
}

object TreeLikeWitness : TreeLikeAdapter<TypeHierarchyTree> {
    override fun getChildren(node: TypeHierarchyTree): List<TypeHierarchyTree> = node.children

    override fun nodeName(node: TypeHierarchyTree): String = node.nodeName
}
