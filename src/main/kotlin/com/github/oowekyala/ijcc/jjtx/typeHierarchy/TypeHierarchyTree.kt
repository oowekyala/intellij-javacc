package com.github.oowekyala.ijcc.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Category.*
import com.github.oowekyala.ijcc.jjtx.JjtxOptsModel
import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.jjtx.JsonPosition
import com.github.oowekyala.ijcc.jjtx.Position
import com.github.oowekyala.ijcc.lang.psi.allJjtreeDecls
import com.github.oowekyala.treeutils.TreeLikeAdapter
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
            TypeHierarchyTree(rootName, JsonPosition("jjtx.typeHierarchy"), emptyList())

        /**
         * First construction pass, from a Json object.
         */
        fun fromJson(json: JsonElement?, ctx: JjtxRunContext): TypeHierarchyTree = when {
            json == null                           -> default()
            json is JsonObject                     -> fromJsonRoot(json, ctx)
            json is JsonPrimitive && json.isString -> default(json.asString)
            else                                   -> {
                ctx.errorCollector.handleError("", WRONG_TYPE, null)
                default()
            }
        }

        private fun fromJsonRoot(jsonObject: JsonObject, ctx: JjtxRunContext): TypeHierarchyTree {

            if (jsonObject.size() > 1) {
                ctx.errorCollector.handleError("${jsonObject.size()}", MULTIPLE_HIERARCHY_ROOTS, null)
                return default()
            } else if (jsonObject.size() == 0) {
                ctx.errorCollector.handleError("", NO_HIERARCHY_ROOTS, null)
                return default()
            }

            val name = jsonObject.keySet().first()
            return jsonObject[name]!!.let { it as? JsonObject }
                ?.let {
                    it.toTree(name, JsonPosition("jjtx.typeHierarchy"), ctx)
                }?.let {
                    it.specificity = Specificity.ROOT
                    it.process(ctx)
                } ?: default(name)
        }

        private fun JsonElement.toTree(nodeName: String,
                                       parentPosition: JsonPosition,
                                       ctx: JjtxRunContext): TypeHierarchyTree? {
            val myPosition = parentPosition.resolve(nodeName)


            val children = when (this) {
                is JsonObject    -> keySet().mapNotNull { name -> this[name].toTree(name, myPosition, ctx) }
                is JsonPrimitive -> emptyList() // TODO
                else             -> emptyList() // TODO

            }

            return TypeHierarchyTree(nodeName, myPosition, children)
        }
    }

}

object TreeLikeWitness : TreeLikeAdapter<TypeHierarchyTree> {
    override fun getChildren(node: TypeHierarchyTree): List<TypeHierarchyTree> = node.children

    override fun nodeName(node: TypeHierarchyTree): String = node.nodeName
}
