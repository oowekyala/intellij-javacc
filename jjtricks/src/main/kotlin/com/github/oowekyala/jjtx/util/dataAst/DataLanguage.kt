package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.jjtx.reporting.JjtricksExceptionWrapper
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.github.oowekyala.jjtx.util.io.extension
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


fun parseGuessFromExtension(input: NamedInputStream, preference: DataLanguage = YamlLang): DataAstNode =
    when (input.extension) {
        "json" -> JsonLang
        "yaml" -> YamlLang
        else   -> preference
    }.parse(input)


internal inline fun <reified T> DataAstNode.load(): T? {
    val type = object : TypeLiteral<T>() {}
    return try {
        Gson().fromJson<Any>(toJson(), type.type) as T?
    } catch (e: Exception) {
        throw JjtricksExceptionWrapper(e, message = null, position = this.position)
    }
}

fun DataAstNode.toYamlString(): String = YamlLang.writeToString(this)
fun DataAstNode.toJsonString(): String = JsonLang.writeToString(this)


fun parseYaml(input: NamedInputStream): DataAstNode = YamlLang.parse(input)

fun parseJson(input: NamedInputStream): DataAstNode = JsonLang.parse(input)
