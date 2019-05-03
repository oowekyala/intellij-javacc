package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.OptsModelImpl
import com.github.oowekyala.jjtx.util.ScalarType.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
import org.yaml.snakeyaml.DumperOptions.ScalarStyle.LITERAL
import org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN
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


fun OptsModelImpl.toDataNode(): DataAstNode {

    val visitors = visitorBeans.toDataNode()

    return AstMap(
        mapOf("jjtx.visitors" to visitors),
        JsonPosition("jjtx")
    )
}

fun DataAstNode.toYaml(): YamlNode = when (this) {
    is AstScalar -> ScalarNode(yamlTag, any, null, null, if ('\n' in any) LITERAL else PLAIN)
    is AstMap    -> MappingNode(yamlTag, map.map { (k, v) -> NodeTuple(Yaml().represent(k), v.toYaml()) }, BLOCK)
    is AstSeq    -> SequenceNode(yamlTag, list.map { it.toYaml() }, BLOCK)
}

val DataAstNode.yamlTag: Tag
    get() = when (this) {
        is AstScalar -> Tag.STR
        is AstMap    -> Tag.MAP
        is AstSeq    -> Tag.SEQ
    }

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


fun OptsModelImpl.toDataMap(): Map<String, Any> {


    val visitors = Yaml().represent(visitorBeans).yamlToData()

    return AstMap(
        mapOf("jjtx.visitors" to visitors),
        JsonPosition("jjtx")
    )
}
