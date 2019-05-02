package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.Position

/**
 * Abstract AST, common denominator between JSON
 * and YAML.
 */
sealed class DataAstNode {
    abstract val position: Position
}


data class AstScalar(
    val any: String,
    val type: ScalarType,
    override val position: Position
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
    override val position: Position
) : DataAstNode(), List<DataAstNode> by list


/**
 * Model of a yaml node / json node.
 *
 * @author Clément Fournier
 */
data class AstMap(
    val map: Map<String, DataAstNode>,
    override val position: Position,
    val namespace: String = ""
) : DataAstNode(), Map<String, DataAstNode> by map {

    companion object {

        operator fun invoke(map: Map<DataAstNode, DataAstNode>, position: Position): AstMap {

            val strMap = map.mapKeysTo(mutableMapOf()) { (k, _) ->
                when {
                    k is AstScalar && k.type == ScalarType.STRING -> k.any
                    else                                          -> null
                }
            }

            strMap.remove(null)

            @Suppress("UNCHECKED_CAST")
            return AstMap(
                map = strMap as Map<String, DataAstNode>,
                position = position
            )
        }
    }

}
