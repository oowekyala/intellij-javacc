package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.google.gson.Gson
import org.apache.commons.lang3.reflect.TypeLiteral
import java.io.StringWriter
import java.io.Writer


/**
 * Represents a language which can be mapped to a [DataAstNode].
 */
interface DataLanguage {

    fun parse(input: NamedInputStream): DataAstNode

    fun write(data: DataAstNode, out: Writer)
}


fun DataLanguage.writeToString(data: DataAstNode): String =
    StringWriter().also {
        write(data, it)
    }.toString()


fun parseGuessFromExtension(input: NamedInputStream, preference: DataLanguage = YAML): DataAstNode =
    when (input.extension) {
        "json" -> JSON.parse(input)
        "yaml" -> YAML.parse(input)
        else   -> preference.parse(input)
    }


internal inline fun <reified T> DataAstNode.load(): T {
    val type = object : TypeLiteral<T>() {}
    val any = Gson().fromJson<Any>(toJson(), type.type)
    return any as T
}

fun parseYaml(input: NamedInputStream): DataAstNode = YAML.parse(input)

fun parseJson(input: NamedInputStream): DataAstNode = JSON.parse(input)
