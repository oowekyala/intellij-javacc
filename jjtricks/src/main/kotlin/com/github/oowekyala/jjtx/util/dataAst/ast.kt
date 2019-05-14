package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.treeutils.TreeLikeAdapter

/**
 * Abstract AST, common denominator between JSON and YAML.
 */
sealed class DataAstNode {
    abstract val position: Position?
}


data class AstScalar(
    val any: String,
    val type: ScalarType,
    override val position: Position? = null
) : DataAstNode() {

    override fun toString(): String = any

}

enum class ScalarType {
    NUMBER,
    STRING,
    BOOLEAN,

    /**
     * Reference to a piece of data defined in another file.
     */
    REFERENCE,

    /**
     * Represents "no value", as in
     *
     *     yaml:
     *
     * The value of the key "yaml" is NULL
     */
    NULL;

    override fun toString(): String = name
}

data class AstSeq(
    val list: List<DataAstNode>,
    override val position: Position? = null
) : DataAstNode(), List<DataAstNode> by list {

    override fun toString(): String = list.toString()
}


data class AstMap(
    val map: Map<String, DataAstNode>,
    val keyPositions: Map<String, Position?> = emptyMap(),
    override val position: Position? = null
) : DataAstNode(), Map<String, DataAstNode> by map {

    override fun toString(): String = map.toString()

    companion object {

        operator fun invoke(map: Map<DataAstNode, DataAstNode>, position: Position? = null): AstMap {

            val strMap = map.mapKeysTo(LinkedHashMap()) { (k, _) ->
                when {
                    k is AstScalar && k.type == ScalarType.STRING -> Pair(k.any, k.position)
                    else                                          -> null
                }
            }

            strMap.remove(null)

            val posMap = strMap.keys.associate { it!! }


            return AstMap(
                map = strMap.mapKeys { (k, _) -> k!!.first },
                keyPositions = posMap,
                position = position
            )
        }
    }
}


object AstTreeLikeWitness : TreeLikeAdapter<DataAstNode> {
    override fun getChildren(node: DataAstNode): List<DataAstNode> = when (node) {
        is AstScalar -> emptyList()
        is AstMap    -> node.values.toList()
        is AstSeq    -> node.list
    }
}
