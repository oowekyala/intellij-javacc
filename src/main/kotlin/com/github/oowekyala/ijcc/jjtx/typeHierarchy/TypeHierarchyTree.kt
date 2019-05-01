package com.github.oowekyala.ijcc.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Category.*
import com.github.oowekyala.ijcc.jjtx.JjtxOptsModel
import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.jjtx.JsonPosition
import com.github.oowekyala.ijcc.jjtx.Position
import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls
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
    specificity: Specificity = Specificity.UNKNOWN
) : TreeOps<TypeHierarchyTree> {

    var specificity: Specificity = specificity
        private set

    override val adapter: TreeLikeAdapter<TypeHierarchyTree> = TreeLikeWitness

    var parent: TypeHierarchyTree? = null
        private set

    private val realChildren: MutableList<TypeHierarchyTree> = children.toMutableList()


    val children: List<TypeHierarchyTree>
        get() = realChildren


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

    fun process(ctx: JjtxRunContext): TypeHierarchyTree {
        val jjtreeDeclsByRawName = ctx.grammarFile.allJjtreeDecls
        val expanded = this.expandAllNames(jjtreeDeclsByRawName.keys, ctx)
        val dedup = expanded.removeDuplicates(ctx)
        val adopted = dedup.adoptOrphansOnRoot(jjtreeDeclsByRawName.values.flatten(), ctx)
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
        fun fromJson(json: JsonElement?, ctx: JjtxRunContext): TypeHierarchyTree = when (json) {
            null -> default()
            else ->
                json.toTree(JsonPosition("jjtx.typeHierarchy"), ctx)
                    ?.also { it.specificity = Specificity.ROOT }
                    ?: default()
        }


        private fun JsonElement.toTree(parentPosition: JsonPosition,
                                       ctx: JjtxRunContext): TypeHierarchyTree? {


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
                                                    ctx: JjtxRunContext): TypeHierarchyTree? {

            val myPosition = parentPosition.resolve(this.toString())
            return if (isString) {
                TypeHierarchyTree(asString, myPosition, emptyList())
            } else {
                ctx.errorCollector.handleError("expected string, got ${this}", WRONG_TYPE, null, myPosition)
                null
            }
        }

        private fun JsonObject.fromJsonObject(parentPosition: JsonPosition,
                                              ctx: JjtxRunContext): TypeHierarchyTree? {

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

            if (children !is JsonArray) {
                ctx.errorCollector.handleError("expected array, got ${children.javaClass}", WRONG_TYPE, null, position)
                return null
            }

            return TypeHierarchyTree(
                nodeName = name,
                children = children.mapNotNull { it.toTree(position, ctx) },
                positionInfo = position
            )
        }

    }

}

object TreeLikeWitness : TreeLikeAdapter<TypeHierarchyTree> {
    override fun getChildren(node: TypeHierarchyTree): List<TypeHierarchyTree> = node.children

    override fun nodeName(node: TypeHierarchyTree): String = node.nodeName
}
