package com.github.oowekyala.ijcc.jjtx

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.Reader
import java.io.StringReader
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author Clément Fournier
 */
class JjtxOptsModel(jsonValue: JsonObject) {

    val nodePrefix: String by jsonValue.string { "AST" }
    val nodePackage: String by jsonValue.string { "" }

    // FIXME by default delegate to root interface
    val typeHierarchy: JsonObject? by jsonValue.obj()

    companion object {


        fun default() : JjtxOptsModel = JjtxOptsModel(JsonObject())

        fun parse(string: String): JjtxOptsModel = parse(StringReader(string))

        fun parse(reader: Reader): JjtxOptsModel =
            JsonParser().parse(reader)?.let { it as? JsonObject }?.let { JjtxOptsModel(it) } ?: default()

        fun parse(file: File): JjtxOptsModel =
            if (!file.exists() || file.isDirectory)
                default()
            else
                parse(file.bufferedReader())

    }
}


private fun JsonObject.string(ns: String = "jjtx", defaultGetter: () -> String): ReadOnlyProperty<Any, String> =
    JsonStringProp(this, ns, defaultGetter)


private fun <T> JsonObject.obj(ns: String = "jjtx",
                               defaultGetter: () -> T,
                               mapper: (JsonObject) -> T): ReadOnlyProperty<Any, T> =
    JsonObjectProp(this, ns, mapper, defaultGetter)


private fun JsonObject.obj(ns: String = "jjtx"): ReadOnlyProperty<Any, JsonObject?> =
    JsonObjectProp(this, ns, { it }, { null })


private class JsonStringProp(val jsonValue: JsonObject, val ns: String, val defaultGetter: () -> String)
    : ReadOnlyProperty<Any, String> {

    override fun getValue(thisRef: Any, property: KProperty<*>) =
        jsonValue["$ns.${property.name}"]?.asString ?: defaultGetter()
}

private class JsonObjectProp<T>(val jsonValue: JsonObject,
                                val ns: String,
                                val mapper: (JsonObject) -> T,
                                val defaultGetter: () -> T)
    : ReadOnlyProperty<Any, T> {

    private lateinit var pname: String
    val myVal: T by lazy {
        jsonValue["$ns.$pname"]?.let { it as? JsonObject }?.let { mapper(it) } ?: defaultGetter()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        pname = property.name
        return myVal
    }

}

fun JsonElement.asObject(): JsonObject? = this as? JsonObject
