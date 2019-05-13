package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.JsonPosition
import com.google.gson.*
import com.google.gson.internal.LazilyParsedNumber


internal fun DataAstNode.toJson(): JsonElement =
    when (this) {

        is AstScalar ->
            when (type) {
                ScalarType.STRING  -> JsonPrimitive(any)
                ScalarType.NUMBER  -> JsonPrimitive(
                    LazilyParsedNumber(
                        any
                    )
                )
                ScalarType.NULL    -> JsonNull.INSTANCE
                ScalarType.BOOLEAN -> JsonPrimitive(any.toBoolean())
            }

        is AstSeq    -> {

            val arr = JsonArray()

            for (elt in list) {
                arr.add(elt.toJson())
            }

            arr
        }

        is AstMap    -> {

            val obj = JsonObject()

            for ((k, v) in map) {
                obj.add(k, v.toJson())
            }

            obj
        }

    }


internal fun JsonElement.jsonToData(): DataAstNode {

    fun JsonElement.jsonReal(parentPosition: JsonPosition): DataAstNode {
        val myPosition = parentPosition.resolve(this.toString())
        return when (this) {
            is JsonNull      -> AstScalar(
                any = toString(),
                type = ScalarType.NULL,
                position = myPosition
            )

            is JsonPrimitive -> {
                val type = when {
                    isNumber -> ScalarType.NUMBER
                    isString -> ScalarType.STRING
                    else     -> ScalarType.BOOLEAN
                }

                AstScalar(
                    any = asString,
                    type = type,
                    position = myPosition
                )
            }

            is JsonArray     -> AstSeq(
                list = this.map { it.jsonReal(myPosition) }.toList(),
                position = myPosition
            )

            is JsonObject    -> {

                val map = entrySet().map {

                    val kPos = myPosition.resolve(it.key)

                    Pair<DataAstNode, DataAstNode>(
                        AstScalar(any = it.key, type = ScalarType.STRING, position = kPos),
                        it.value.jsonReal(kPos)
                    )
                }.toMap()


                AstMap(
                    map = map,
                    position = myPosition
                )
            }

            else             -> throw IllegalStateException("Unknown node type $this")
        }


    }

    return jsonReal(JsonPosition(emptyList()))
}
