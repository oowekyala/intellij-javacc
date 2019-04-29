package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Category.*
import com.github.oowekyala.ijcc.jjtx.ErrorCollector.Severity.FAIL
import com.github.oowekyala.treeutils.TreeLikeAdapter
import tv.twelvetone.json.JsonObject

/**
 * @author Clément Fournier
 */
data class TypeHierarchyTree(val nodeName: String,
                             val positionInfo: SourcePosition?,
                             val children: List<TypeHierarchyTree>) {

    private var parent: TypeHierarchyTree? = null
        private set


    /**
     * Unescaped the node shorthands in the type hierarchy and produces a new
     * root.
     *
     * Shorthands are
     *
     * "regex(something)" -> match all nodes with regex, can only be a leaf pattern, then expanded to package + prefix + name
     * "Something"        -> expanded to eg package.ASTSomething
     * "%Something"       -> expanded to package.Something
     * "foo.Something"    -> exactly foo.Something
     *
     */
    fun expandRegex(grammarNodeNames: Set<String>,
                    ctx: JjtxRunContext): List<TypeHierarchyTree> {


        regexThRule.matchEntire(nodeName)?.groups?.get(1)?.run {
            return expandRegex(grammarNodeNames, Regex(value), ctx) // TODO invalid regex?
        }


        val realName = when {
            nodeName[0] == '%' -> ctx.jjtxOptsModel.nodePackage + "." + nodeName.substring(1)
            nodeName.matches(Regex("\\w+")) -> ctx.jjtxOptsModel.nodePackage + "." + ctx.jjtxOptsModel.nodePrefix + nodeName
            else -> nodeName
        }

        if (realName !in grammarNodeNames) {
            ctx.errorCollector.handleError(realName, UNMATCHED_EXACT_NODE, sourcePosition = positionInfo)
        }

        return listOf(TypeHierarchyTree(
            realName,
            positionInfo,
            children.flatMap { expandRegex(grammarNodeNames, ctx) }
        ))
    }

    private fun expandRegex(grammarNodeNames: Set<String>,
                            extractedRegex: Regex,
                            ctx: JjtxRunContext): List<TypeHierarchyTree> {


        val matching = grammarNodeNames.filter { extractedRegex.matches(it) }


        if (children.isNotEmpty()) {
            val override = if (matching.size == 1) null else FAIL
            ctx.errorCollector.handleError(
                extractedRegex.pattern,
                REGEX_SHOULD_BE_LEAF,
                severityOverride = override,
                sourcePosition = positionInfo
            )
            if (override == FAIL) {
                return emptyList()
            }
        }


        if (matching.isEmpty()) {
            ctx.errorCollector.handleError(
                extractedRegex.pattern,
                UNMATCHED_HIERARCHY_REGEX,
                sourcePosition = positionInfo
            )
        }
        return matching.map {
            TypeHierarchyTree(it, positionInfo, emptyList())
        }
    }

    companion object {

        private val regexThRule = Regex("^regex\\(.*\\)$")


        fun fromJsonRoot(jsonObject: JsonObject, ctx: JjtxRunContext): TypeHierarchyTree? {

            if (jsonObject.size() > 1) {
                ctx.errorCollector.handleError("${jsonObject.size()}", MULTIPLE_HIERARCHY_ROOTS, null, null)
                return null
            } else if (jsonObject.size() == 0) {
                ctx.errorCollector.handleError("", NO_HIERARCHY_ROOTS, null, null)
                return null
            }

            val name = jsonObject.names()[0]
            return fromJsonObjectImpl(name, jsonObject[name]!!.asObject(), ctx)
        }

        private fun fromJsonObjectImpl(nodeName: String,
                                       jsonObject: JsonObject,
                                       ctx: JjtxRunContext): TypeHierarchyTree {
            val me = TypeHierarchyTree(
                nodeName,
                null,
                jsonObject.names().map { fromJsonObjectImpl(it, jsonObject[it]!!.asObject(), ctx) }
            )

            me.children.forEach { it.parent = me }
            return me
        }
    }
}

object TreeLikeWitness : TreeLikeAdapter<TypeHierarchyTree> {
    override fun getChildren(node: TypeHierarchyTree): List<TypeHierarchyTree> = node.children

    override fun nodeName(node: TypeHierarchyTree): String = node.nodeName
}
