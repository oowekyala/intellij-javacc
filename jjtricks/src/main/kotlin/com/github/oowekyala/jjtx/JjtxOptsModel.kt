package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.jjtx.templates.GrammarGenerationScheme
import com.github.oowekyala.jjtx.templates.NodeBean
import com.github.oowekyala.jjtx.templates.VisitorGenerationTask
import com.github.oowekyala.jjtx.util.*
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.reader.UnicodeReader
import java.io.Reader
import java.nio.file.Path

/**
 * Models a jjtopts configuration file.
 *
 * @author Clément Fournier
 */
interface JjtxOptsModel : IGrammarOptions {

    /**
     * The parent model in the chain. Properties that
     * are not found in this model are delegated to the parent.
     */
    val parentModel: JjtxOptsModel?

    override val nodePrefix: String

    override val nodePackage: String

    override val isDefaultVoid: Boolean

    /**
     * The fully resolved type hierarchy tree,
     * not inherited.
     */
    val typeHierarchy: NodeBean

    /**
     * Global template variables, merged with the parent maps.
     */
    val templateContext: Map<String, Any>

    /**
     * Map of ids to runnable visitor generation tasks.
     */
    val visitors: Map<String, VisitorGenerationTask>

    /**
     * The node generation scheme, not merged if provided.
     */
    val grammarGenerationSchemes: Map<String, GrammarGenerationScheme>


    val activeNodeGenerationScheme: String?


    companion object {

        const val DefaultRootNodeName = "Node"

        /**
         * Parses and chains a full chain of paths, throwing an
         * exception if one model can't be parsed. The context
         * must be initialised (constructor must have completed).
         *
         * @param ctx Context
         * @param configChain In decreasing precedence order. The default root file
         *                    and the inline options will be added to the top of the chain.
         *
         */
        fun parseChain(ctx: JjtxContext, configChain: List<Path>): JjtxOptsModel =
            // they're in decreasing precedence order
            configChain
                .filter { it.isFile() }
                .map { NamedInputStream(it.inputStream(), it.toString()) }
                .plus(RootJjtOpts)
                // but we fold them from least important to most important
                .asReversed()
                .fold<NamedInputStream, JjtxOptsModel>(OldJavaccOptionsModel(ctx.grammarFile)) { model, path ->
                    try {
                        parse(ctx, path, model)
                    } catch (e: Exception) {
                        throw RuntimeException("Exception parsing file ${path.filename} : ${e.message}")
                    }
                }

        /**
         * Parse a single jjtopts [file] into a [JjtxOptsModel].
         * Supported models are JSON and Yaml. Yaml is much more
         * convenient, if only because it allows inputting multiline
         * strings.
         *
         * @throws Exception on parsing errors.
         */
        fun parse(ctx: JjtxContext,
                  file: NamedInputStream,
                  parent: JjtxOptsModel): JjtxOptsModel {

            return when (file.extension) {
                "json" -> parseJson(ctx, file.inputStream.bufferedReader(), parent)
                // by default assume it's yaml
                else   -> parseYaml(ctx, file, parent)
            }
        }


        private fun parseYaml(ctx: JjtxContext,
                              file: NamedInputStream,
                              parent: JjtxOptsModel): JjtxOptsModel {

            val reader = UnicodeReader(file.inputStream).buffered()

            val json = Yaml().compose(reader).yamlToData(file.filename)

            return fromElement(ctx, json, parent)
        }

        private fun parseJson(ctx: JjtxContext,
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

