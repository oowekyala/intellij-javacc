package com.github.oowekyala.jjtx.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.Reader
import java.nio.file.Path
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun String.splitAroundFirst(delimiter: Char): Pair<String, String> =
    Pair(substringBefore(delimiter, missingDelimiterValue = ""), substringAfter(delimiter))

/**
 * Returns a pair of the substrings before and after the last occurrence of the [delimiter].
 * If the delimiter is not found in the string, then
 * - if [firstBias], returns a Pair([this], ""),
 * - else returns Pair("", [this])
 */
fun String.splitAroundLast(delimiter: Char, firstBias: Boolean = false): Pair<String, String> =
    Pair(
        substringBeforeLast(delimiter, missingDelimiterValue = if (firstBias) this else ""),
        substringAfterLast(delimiter, missingDelimiterValue = if (firstBias) "" else this)
    )

val Path.extension: String?
    get() = toFile().extension.takeIf { it.isNotEmpty() } ?: null

fun Path.bufferedReader(): Reader = toFile().bufferedReader()


fun JsonObject.asMap(): Map<String, JsonElement> {
    return object : Map<String, JsonElement> {
        val obj = this@asMap

        override val keys: Set<String>
            get() = obj.keySet()
        override val size: Int
            get() = obj.size()
        override val values: Collection<JsonElement>
            get() = obj.entrySet().map { it.value }

        override fun containsKey(key: String): Boolean = obj.has(key)

        override fun containsValue(value: JsonElement): Boolean =
            obj.entrySet().any {
                it.value == value
            }

        override fun get(key: String): JsonElement? = obj[key]

        override fun isEmpty(): Boolean = obj.keySet().isEmpty()

        override val entries: Set<Map.Entry<String, JsonElement>>
            get() = obj.entrySet()

    }
}


inline fun <reified T> ReadOnlyProperty<Any, *>.coerce(): ReadOnlyProperty<Any, T?> =
    object : ReadOnlyProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? =
            this@coerce.getValue(thisRef, property) as? T
    }


fun <R : Any, T> ReadOnlyProperty<R, T>.lazily(): ReadOnlyProperty<R, T> =
    object : ReadOnlyProperty<R, T> {

        private lateinit var myThisRef: R
        private lateinit var myProperty: KProperty<*>

        val v: T by lazy {
            this@lazily.getValue(myThisRef, myProperty)
        }

        override fun getValue(thisRef: R, property: KProperty<*>): T {
            myThisRef = thisRef
            myProperty = property
            return v
        }
    }


fun <T, R> ReadOnlyProperty<Any, T>.map(f: (T) -> R): ReadOnlyProperty<Any, R> =
    object : ReadOnlyProperty<Any, R> {
        override fun getValue(thisRef: Any, property: KProperty<*>): R =
            f(this@map.getValue(thisRef, property))
    }
