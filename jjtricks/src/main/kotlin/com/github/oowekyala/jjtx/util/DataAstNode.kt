package com.github.oowekyala.jjtx.util

import java.util.*

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
) : DataAstNode()

enum class ScalarType {
    NUMBER,
    STRING,
    BOOLEAN,
    NULL;

    override fun toString(): String = name
}

data class AstSeq(
    val list: List<DataAstNode>,
    override val position: Position? = null
) : DataAstNode(), List<DataAstNode> by list


data class AstMap(
    val map: Map<String, DataAstNode>,
    override val position: Position? = null
) : DataAstNode(), Map<String, DataAstNode> by map {

    companion object {

        operator fun invoke(map: Map<DataAstNode, DataAstNode>, position: Position? = null): AstMap {

            val strMap = map.mapKeysTo(TreeMap(String::compareTo)) { (k, _) ->
                when {
                    k is AstScalar && k.type == ScalarType.STRING -> k.any
                    else                                          -> null
                }
            }

            //            strMap.remove(null)

            @Suppress("UNCHECKED_CAST")
            return AstMap(
                map = strMap as Map<String, DataAstNode>,
                position = position
            )
        }
    }

}
