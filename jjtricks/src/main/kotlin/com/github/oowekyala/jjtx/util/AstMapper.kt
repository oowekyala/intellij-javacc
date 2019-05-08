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
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.LinkedHashMap
import org.yaml.snakeyaml.nodes.Node as YamlNode


internal val YamlNode.position: YamlPosition
    get() = YamlPosition(startMark, endMark)

internal fun YamlNode.yamlToData(fileName: String? = null): DataAstNode =
    when (this) {
        is ScalarNode   ->
            AstScalar(
                any = value,
                type = STRING,
                position = position.addName(fileName)
            )
        is SequenceNode ->
            AstSeq(
                list = value.map { it.yamlToData(fileName) },
                position = position.addName(fileName)
            )
        is MappingNode  -> {
            // preserve order!
            val values =
                this.value.map {
                    Pair(it.keyNode.yamlToData(fileName), it.valueNode.yamlToData(fileName))
                }.toMap(LinkedHashMap())

            AstMap(
                map = values,
                position = position.addName(fileName)
            )
        }
        else            -> throw IllegalStateException("Unknown node type")
    }


internal fun JsonElement.jsonToData(): DataAstNode {

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

            else             -> throw IllegalStateException("Unknown node type $this")
        }


    }

    return jsonReal(JsonPosition(emptyList()))
}



internal fun DataAstNode.toJson(): JsonElement =
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


internal fun OptsModelImpl.toDataNode(): DataAstNode {

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

internal fun DataAstNode.toYaml(): YamlNode = when (this) {
    is AstScalar -> ScalarNode(
        yamlTag,
        any,
        null,
        null,
        if ('\n' in any) DumperOptions.ScalarStyle.LITERAL else DumperOptions.ScalarStyle.PLAIN
    )
    is AstMap    -> MappingNode(
        yamlTag, map.map { (k, v) -> NodeTuple(Yaml().represent(k), v.toYaml()) }.sortedBy { (it.keyNode as ScalarNode).value },
        DumperOptions.FlowStyle.BLOCK
    )
    is AstSeq    -> SequenceNode(yamlTag, list.map { it.toYaml() }, DumperOptions.FlowStyle.BLOCK)
}

internal val DataAstNode.yamlTag: Tag
    get() = when (this) {
        is AstScalar -> Tag.STR
        is AstMap    -> Tag.MAP
        is AstSeq    -> Tag.SEQ
    }

internal fun TypeHierarchyTree.toDataNode(): DataAstNode =
    if (children.isEmpty())
        AstScalar(nodeName, STRING)
    else
        AstMap(mapOf(nodeName to AstSeq(children.map { it.toDataNode() })))

internal fun YamlNode.toYamlString(): String {
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


internal fun OptsModelImpl.toYaml(): YamlNode = toDataNode().toYaml()

internal fun Any?.toDataNode(): DataAstNode {
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

private inline fun <reified T : Any> T.propertiesMap(): Map<String, DataAstNode> =
    javaClass.properties.map { Pair(it.name, it.getter(this).toDataNode()) }.toMap()


// avoids endless loop in the case of cycle
private val propertyMapCache = WeakHashMap<Class<*>, List<JProperty<*, *>>>()

private val <T : Any> Class<T>.properties: List<JProperty<T, *>>
    get() = propertyMapCache.computeIfAbsent(this) {
        methods.toList().mapNotNull { JProperty.toPropertyOrNull(it) }
    } as List<JProperty<T, *>>

private class JProperty<in T : Any, out V> private constructor(
    val name: String,
    val getter: (T) -> V?
) {


    companion object {

        private val GetterRegex = Regex("get([A-Z].*)")
        private val BoolGetterRegex = Regex("is[A-Z].*")


        fun toPropertyOrNull(method: Method): JProperty<*, *>? {
            return method.propertyName?.let {
                JProperty<Any, Any?>(it) {
                    try {
                        method.invoke(it)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

        private val Method.propertyName: String?
            get() {

                if (this == java.lang.Object::class.java.getDeclaredMethod("getClass")) return null

                if (parameterCount != 0 || isBridge) return null

                GetterRegex.matchEntire(name)?.groupValues?.get(1)?.let {
                    return it.decapitalize()
                }

                if (returnType == Boolean::class.java || returnType == java.lang.Boolean.TYPE) {
                    BoolGetterRegex.matchEntire(name)?.groupValues?.get(0)?.let {
                        return it.decapitalize()
                    }
                }
                return null
            }

    }
}
