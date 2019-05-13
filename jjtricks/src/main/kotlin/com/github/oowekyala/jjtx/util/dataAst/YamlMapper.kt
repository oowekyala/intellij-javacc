package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.YamlPosition
import com.github.oowekyala.jjtx.util.addName
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.emitter.Emitter
import org.yaml.snakeyaml.error.YAMLException
import org.yaml.snakeyaml.nodes.*
import org.yaml.snakeyaml.resolver.Resolver
import org.yaml.snakeyaml.serializer.Serializer
import java.io.IOException
import java.io.StringWriter

/**
 * @author Clément Fournier
 */

internal val Node.position: YamlPosition
    get() = YamlPosition(startMark, endMark)

internal fun Node.yamlToData(fileName: String? = null): DataAstNode =
    when (this) {
        is ScalarNode   ->
            AstScalar(
                any = value,
                type = ScalarType.STRING,
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


internal fun DataAstNode.toYaml(): Node = when (this) {
    is AstScalar -> ScalarNode(
        yamlTag,
        any,
        null,
        null,
        if ('\n' in any) DumperOptions.ScalarStyle.LITERAL else DumperOptions.ScalarStyle.PLAIN
    )
    is AstMap    -> MappingNode(
        yamlTag,
        map.map { (k, v) -> NodeTuple(Yaml().represent(k), v.toYaml()) }.sortedBy { (it.keyNode as ScalarNode).value },
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
        AstScalar(nodeName, ScalarType.STRING)
    else
        AstMap(
            mapOf(
                nodeName to AstSeq(
                    children.map { it.toDataNode() })
            )
        )

internal fun Node.toYamlString(): String {
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

