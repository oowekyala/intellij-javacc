package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.JsonPointer
import com.github.oowekyala.jjtx.util.dataAst.ScalarType.*
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.google.gson.*
import com.google.gson.internal.LazilyParsedNumber
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonReader
import java.io.IOException
import java.io.Writer

/**
 * TODO use org.json and remove Gson. org.json has no deserializer though
 */
object JsonLang : DataLanguage {
    override fun parse(input: NamedInputStream): DataAstNode =
        input.newInputStream().bufferedReader().use { reader ->
            val jsonReader = JsonReader(reader).apply {
                isLenient = true
            }

            JsonParser().parse(jsonReader).toData()
        }

    override fun write(data: DataAstNode, out: Writer) {
        try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonWriter = gson.newJsonWriter(out)
            jsonWriter.isLenient = true
            Streams.write(data.toJson(), jsonWriter)
        } catch (e: IOException) {
            throw AssertionError(e)
        }
    }
}

internal fun DataAstNode.toJson(): JsonElement =
    when (this) {

        is AstScalar ->
            when (type) {
                STRING, REFERENCE -> JsonPrimitive(any)
                NUMBER            -> JsonPrimitive(LazilyParsedNumber(any))
                NULL              -> JsonNull.INSTANCE
                BOOLEAN           -> JsonPrimitive(any.toBoolean())
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


private fun JsonElement.toData(): DataAstNode {

    fun JsonElement.jsonReal(parentPointer: JsonPointer): DataAstNode {
        val myPosition = parentPointer.resolve(this.toString())
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

                    val kPos = myPosition / it.key

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

            else             -> throw IllegalStateException("Unknown node type $this")
        }


    }

    return jsonReal(JsonPointer.Root)
}
