package com.github.oowekyala.ijcc.jjtx.typeHierarchy

import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Category.MULTIPLE_HIERARCHY_ROOTS
import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Category.NO_HIERARCHY_ROOTS
import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.jjtx.JsonPosition
import com.github.oowekyala.ijcc.jjtx.Position
import com.github.oowekyala.ijcc.jjtx.asObject
import com.github.oowekyala.ijcc.lang.psi.JjtNodeClassOwner
import com.github.oowekyala.treeutils.TreeLikeAdapter
import com.google.gson.JsonObject

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



    companion object {


        fun buildFully(jsonObject: JsonObject,
                       jjtreeDeclsByRawName: Map<String, List<JjtNodeClassOwner>>,
                       ctx: JjtxRunContext): TypeHierarchyTree? {

            val fst = fromJsonRoot(jsonObject, ctx) ?: return null
            val expanded = fst.expandAllNames(jjtreeDeclsByRawName.keys, ctx)
            val dedup = expanded.removeDuplicates(ctx)
            val adopted = dedup.adoptOrphansOnRoot(jjtreeDeclsByRawName.values.flatten(), ctx)
            return adopted
        }


        /**
         * First construction pass, from a Json object.
         */
        private fun fromJsonRoot(jsonObject: JsonObject, ctx: JjtxRunContext): TypeHierarchyTree? {

            if (jsonObject.size() > 1) {
                ctx.errorCollector.handleError("${jsonObject.size()}", MULTIPLE_HIERARCHY_ROOTS, null)
                return null
            } else if (jsonObject.size() == 0) {
                ctx.errorCollector.handleError("", NO_HIERARCHY_ROOTS, null)
                return null
            }

            val name = jsonObject.keySet().first()
            return jsonObject[name]!!.asJsonObject
                ?.let {
                    fromJsonObjectImpl(
                        name,
                        JsonPosition("jjtx.typeHierarchy"),
                        it,
                        ctx
                    )
                }?.also {
                    it.specificity = Specificity.ROOT
                }
        }

        private fun fromJsonObjectImpl(nodeName: String,
                                       parentPosition: JsonPosition,
                                       jsonObject: JsonObject,
                                       ctx: JjtxRunContext): TypeHierarchyTree {
            val myPosition = parentPosition.resolve(nodeName)

            return TypeHierarchyTree(
                nodeName,
                myPosition,
                jsonObject.keySet().mapNotNull { name ->
                    jsonObject[name]!!.asObject()?.let {
                        fromJsonObjectImpl(
                            name,
                            myPosition,
                            it,
                            ctx
                        )
                    }
                }
            )
        }
    }
}

object TreeLikeWitness : TreeLikeAdapter<TypeHierarchyTree> {
    override fun getChildren(node: TypeHierarchyTree): List<TypeHierarchyTree> = node.children

    override fun nodeName(node: TypeHierarchyTree): String = node.nodeName
}
