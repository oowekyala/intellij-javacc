package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.dataAst.DataLanguage.JSON
import com.github.oowekyala.jjtx.util.dataAst.DataLanguage.YAML
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.reader.UnicodeReader


enum class DataLanguage {
    YAML {

        override fun parse(input: NamedInputStream): DataAstNode =
            input.newInputStream().use { istream ->
                val reader = UnicodeReader(istream).buffered()
                Yaml().compose(reader).yamlToData(input.filename)
            }
    },
    JSON {
        override fun parse(input: NamedInputStream): DataAstNode =
            input.newInputStream().bufferedReader().use { reader ->
                val jsonReader = JsonReader(reader).apply {
                    isLenient = true
                }

                JsonParser().parse(jsonReader).jsonToData()
            }

    };

    abstract fun parse(input: NamedInputStream): DataAstNode
}

fun parseGuessFromExtension(input: NamedInputStream, preference: DataLanguage = YAML): DataAstNode =
    when (input.extension) {
        "json" -> JSON.parse(input)
        "yaml" -> YAML.parse(input)
        else   -> preference.parse(input)
    }


fun parseYaml(input: NamedInputStream): DataAstNode =
    YAML.parse(input)

fun parseJson(input: NamedInputStream): DataAstNode =
    JSON.parse(input)
