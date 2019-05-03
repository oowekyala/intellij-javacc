package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.util.ScalarType.*
import com.google.gson.*
import com.google.gson.internal.LazilyParsedNumber
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.SequenceNode
import org.yaml.snakeyaml.nodes.Node as YamlNode


val YamlNode.position: Position
    get() = YamlPosition(startMark, endMark)

fun YamlNode.yamlToData(): DataAstNode =
    when (this) {
        is ScalarNode   ->
            AstScalar(
                any = value,
                type = STRING,
                position = position
            )
        is SequenceNode ->
            AstSeq(
                list = value.map { it.yamlToData() },
                position = position
            )
        is MappingNode  ->
            AstMap(
                map = this.value.map { Pair(it.keyNode.yamlToData(), it.valueNode.yamlToData()) }.toMap(),
                position = position
            )
        else            -> throw IllegalStateException("Unknown node type")
    }


fun JsonElement.jsonToData(): DataAstNode {

    fun JsonElement.jsonReal(parentPosition: JsonPosition): DataAstNode {
        val myPosition = parentPosition.resolve(this.toString())
        return when (this) {
            is JsonNull      -> AstScalar(
                any = toString(),
                type = NULL,
                position = myPosition
            )

            is JsonPrimitive -> {
                val type = when {
                    isNumber -> NUMBER
                    isString -> STRING
                    else     -> BOOLEAN
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
                        AstScalar(any = it.key, type = STRING, position = kPos),
                        it.value.jsonReal(kPos)
                    )
                }.toMap()


                AstMap(
                    map = map,
                    position = myPosition
                )
            }

            else             -> throw IllegalStateException("Unknown node type ${this.toString()}")
        }


    }

    return jsonReal(JsonPosition(emptyList()))
}



fun DataAstNode.toJson(): JsonElement =
    when (this) {

        is AstScalar ->
            when (type) {
                STRING  -> JsonPrimitive(any)
                NUMBER  -> JsonPrimitive(LazilyParsedNumber(any))
                NULL    -> JsonNull()
                BOOLEAN -> JsonPrimitive(any.toBoolean())
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
