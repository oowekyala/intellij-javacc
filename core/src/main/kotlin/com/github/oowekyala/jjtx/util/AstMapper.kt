package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.ScalarType.*
import com.google.gson.*
import com.google.gson.internal.LazilyParsedNumber
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.emitter.Emitter
import org.yaml.snakeyaml.error.YAMLException
import org.yaml.snakeyaml.nodes.*
import org.yaml.snakeyaml.resolver.Resolver
import org.yaml.snakeyaml.serializer.Serializer
import java.io.IOException
import java.io.StringWriter
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
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


fun OptsModelImpl.toDataNode(): DataAstNode {

    val visitors = visitorBeans.toDataNode()

    val th = typeHierarchy.toDataNode()

    return AstMap(
        mapOf(
            "jjtx" to AstMap(
                mapOf(
                    "nodePrefix" to nodePrefix.toDataNode(),
                    "nodePackage" to nodePackage.toDataNode(),
                    "visitors" to visitors,
                    "templateContext" to templateContext.toDataNode(),
                    "typeHierarchy" to th
                )
            )
        )
    )
}

fun DataAstNode.toYaml(): YamlNode = when (this) {
    is AstScalar -> ScalarNode(
        yamlTag,
        any,
        null,
        null,
        if ('\n' in any) DumperOptions.ScalarStyle.LITERAL else DumperOptions.ScalarStyle.PLAIN
    )
    is AstMap    -> MappingNode(
        yamlTag, map.map { (k, v) -> NodeTuple(Yaml().represent(k), v.toYaml()) },
        DumperOptions.FlowStyle.BLOCK
    )
    is AstSeq    -> SequenceNode(yamlTag, list.map { it.toYaml() }, DumperOptions.FlowStyle.BLOCK)
}

val DataAstNode.yamlTag: Tag
    get() = when (this) {
        is AstScalar -> Tag.STR
        is AstMap    -> Tag.MAP
        is AstSeq    -> Tag.SEQ
    }

fun TypeHierarchyTree.toDataNode(): DataAstNode =
    if (children.isEmpty())
        AstScalar(nodeName, STRING)
    else
        AstMap(mapOf(nodeName to AstSeq(children.map { it.toDataNode() })))

fun YamlNode.toYamlString(): String {
    val opts = DumperOptions()
    val sw = StringWriter()
    val serializer = Serializer(Emitter(sw, opts), Resolver(), opts, Tag.MAP)
    try {
        serializer.open()
        serializer.serialize(this)
        serializer.close()
    } catch (var6: IOException) {
        throw YAMLException(var6)
    }

    return sw.toString()
}


fun OptsModelImpl.toYaml(): YamlNode = toDataNode().toYaml()

fun Any?.toDataNode(): DataAstNode {
    return when (this) {
        null             -> AstScalar("null", NULL)
        is String        -> AstScalar(this, STRING)
        is Number        -> AstScalar(this.toString(), NUMBER)
        is Boolean       -> AstScalar(this.toString(), BOOLEAN)
        is Collection<*> -> AstSeq(this.map { it.toDataNode() })
        is Map<*, *>     ->
            AstMap(
                this.mapNotNull { (k, v) ->
                    v?.let { Pair(k.toDataNode(), v.toDataNode()) }
                }.toMap()
            )
        else             -> {
            AstMap(this.propertiesMap().filterValues { it !is AstScalar || it.type != NULL })
        }
    }
}

private inline fun <reified T : Any> T.propertiesMap(): Map<String, DataAstNode> {
    val kclass: KClass<T> = this::class as KClass<T>
    return kclass.memberProperties.map { Pair(it.name, it.get(this).toDataNode()) }.toMap()
}