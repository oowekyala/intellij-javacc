package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.jjtx.util.toFilePos
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.emitter.Emitter
import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.error.YAMLException
import org.yaml.snakeyaml.nodes.*
import org.yaml.snakeyaml.resolver.Resolver
import org.yaml.snakeyaml.serializer.Serializer
import java.io.IOException
import java.io.StringWriter

/**
 * @author Clément Fournier
 */

internal fun Node.position(filename: String?): Position? = startMark?.toFilePos(filename)

/**
 * Converts a Yaml node to the language independent AST.
 *
 * @param fileName Name of the file from which the node was parsed.
 * SnakeYaml's [Mark]s have a dummy string by default and must be
 * replaced.
 */
internal fun Node.yamlToData(fileName: String? = null): DataAstNode =
    when (this) {
        is ScalarNode   -> {
            AstScalar(
                any = value,
                type = tag.scalarType,
                position = position(fileName)
            )
        }
        is SequenceNode ->
            AstSeq(
                list = value.map { it.yamlToData(fileName) },
                position = position(fileName)
            )
        is MappingNode  -> {
            // preserve order!
            val values =
                this.value.map {
                    Pair(it.keyNode.yamlToData(fileName), it.valueNode.yamlToData(fileName))
                }.toMap(LinkedHashMap())

            AstMap(
                map = values,
                position = position(fileName)
            )
        }
        else            -> throw IllegalStateException("Unknown node type")
    }

internal fun DataAstNode.toYamlString(): String = toYaml().toYamlString()

/**
 * Dumps the node to yaml.
 *
 * The reconstructed yaml tree has no position data.
 * It's possible to rebuild positions by dumping to
 * a string ([toYamlString]) and reparsing.
 */
private fun DataAstNode.toYaml(): Node = when (this) {
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

private val DataAstNode.yamlTag: Tag
    get() = when (this) {
        is AstScalar -> Tag.STR
        is AstMap    -> Tag.MAP
        is AstSeq    -> Tag.SEQ
    }

private val Tag.scalarType: ScalarType
    get() = when (this) {
        Tag.BOOL           -> ScalarType.BOOLEAN
        Tag.INT, Tag.FLOAT -> ScalarType.NUMBER
        Tag.NULL           -> ScalarType.NULL
        else               -> ScalarType.STRING
    }


private fun Node.toYamlString(): String {
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

