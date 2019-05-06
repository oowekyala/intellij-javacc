package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.jjtx.templates.VisitorGenerationTask
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.*
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.reader.UnicodeReader
import java.io.Reader

/**
 * Models a jjtopts configuration file.
 *
 * @author Clément Fournier
 */
interface JjtxOptsModel : IGrammarOptions {

    val parentModel: JjtxOptsModel?
    override val nodePrefix: String
    override val nodePackage: String
    override val isDefaultVoid: Boolean
    val typeHierarchy: TypeHierarchyTree

    val templateContext: Map<String, Any>

    val visitors: Map<String, VisitorGenerationTask>

    companion object {

        const val DefaultRootNodeName = "Node"

        /**
         * Throws exceptions on parsing errors.
         */
        fun parse(ctx: JjtxContext,
                  file: NamedInputStream,
                  parent: JjtxOptsModel): JjtxOptsModel {

            return when (file.extension) {
                "json" -> parseJson(ctx, file.inputStream.bufferedReader(), parent)
                // by default assume it's yaml
                else   -> parseYaml(ctx, UnicodeReader(file.inputStream).buffered(), parent)
            }
        }


        fun parseYaml(ctx: JjtxContext,
                      reader: Reader,
                      parent: JjtxOptsModel): JjtxOptsModel {

            val json = Yaml().compose(reader).yamlToData()

            return fromElement(ctx, json, parent)
        }

        fun parseJson(ctx: JjtxContext,
                      reader: Reader,
                      parent: JjtxOptsModel): JjtxOptsModel {

            val jsonReader = JsonReader(reader)
            jsonReader.isLenient = true

            val jsonParser = JsonParser()

            val elt = jsonParser.parse(jsonReader).jsonToData()

            return fromElement(ctx, elt, parent)
        }

        private fun fromElement(ctx: JjtxContext,
                                jsonElement: DataAstNode,
                                parent: JjtxOptsModel) =
            OptsModelImpl(ctx, parent, jsonElement.let { it as AstMap })

    }
}


fun JjtxOptsModel.addPackage(simpleName: String) =
    nodePackage.let { if (it.isNotEmpty()) "$it.$simpleName" else simpleName }

