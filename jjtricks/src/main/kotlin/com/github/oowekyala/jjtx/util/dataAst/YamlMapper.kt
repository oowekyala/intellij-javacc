package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.Position
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.github.oowekyala.jjtx.util.toFilePos
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.emitter.Emitter
import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.nodes.*
import org.yaml.snakeyaml.reader.UnicodeReader
import org.yaml.snakeyaml.resolver.Resolver
import org.yaml.snakeyaml.serializer.Serializer
import java.io.IOException
import java.io.Writer


object YamlLang : DataLanguage {

    override fun parse(input: NamedInputStream): DataAstNode =
        input.newInputStream().use { istream ->
            val reader = UnicodeReader(istream).buffered()
            Yaml().compose(reader).yamlToData(input.filename)
        }

    override fun write(data: DataAstNode, out: Writer) {
        val opts = DumperOptions()
        val serializer = Serializer(Emitter(out, opts), Resolver(), opts, Tag.MAP)
        try {
            serializer.open()
            serializer.serialize(data.toYaml())
            serializer.close()
        } catch (var6: IOException) {
            throw RuntimeException("Exception dumping node to yaml", var6)
        }
    }
}
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

private val RefTag = Tag("!from")

private val Tag.scalarType: ScalarType
    get() = when (this) {
        Tag.BOOL           -> ScalarType.BOOLEAN
        Tag.INT, Tag.FLOAT -> ScalarType.NUMBER
        Tag.NULL           -> ScalarType.NULL
        RefTag             -> ScalarType.REFERENCE
        else               -> ScalarType.STRING
    }
