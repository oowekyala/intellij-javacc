package com.github.oowekyala.jjtx.util

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


fun DataAstNode.prettyPrint(indent: String = "    "): String =
    StringBuilder().also {
        this.prettyPrintImpl(indent, "", it)
    }.toString()

private fun DataAstNode?.prettyPrintImpl(baseIndent: String, indent: String, sb: StringBuilder) {
    val inindent = indent + baseIndent

    when (this) {
        is AstMap    -> {
            if (this.isEmpty()) sb.append("{}")
            else {
                sb.appendln("{")
                for ((k, v) in this) {
                    sb.append(inindent).append('"').append(k).append('"').append(": ")
                    v.prettyPrintImpl(baseIndent, inindent + baseIndent, sb)
                    sb.appendln()
                }
                sb.append(indent).appendln("}")

            }
        }

        is AstSeq    -> {
            if (this.isEmpty()) sb.append("[]")
            else {
                sb.appendln("[")
                for (v in this) {
                    sb.append(inindent)
                    v.prettyPrintImpl(baseIndent, inindent, sb)
                    sb.appendln()
                }
                sb.append(indent).appendln("]")
            }
        }
        is AstScalar -> {
            if (any.any { it == '\r' || it == '\n' }) {
                sb.appendln()
                sb.append(any.replaceIndent(inindent))
            } else {
                sb.append(any)
            }

        }
    }
}
