package com.github.oowekyala.jjtx.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.apache.commons.lang3.text.WordUtils
import java.io.FileInputStream
import java.io.InputStream
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
    get() = toFile().extension.takeIf { it.isNotEmpty() }

val Path.baseName: String
    get() = fileName.toString().substringBefore('.')

fun Path.bufferedReader(): Reader = toFile().bufferedReader()
fun Path.inputStream(): InputStream = FileInputStream(toFile())
fun Path.isDirectory() : Boolean = Files.isDirectory(this)
fun Path.exists() : Boolean = Files.exists(this)
fun Path.isFile() : Boolean = Files.isRegularFile(this)
fun Path.createFile() {
    Files.createDirectories(parent)
    Files.createFile(this)
}

fun String.wrap(lineLength: Int, indent: Int = 0): String =
    WordUtils.wrap(this, lineLength, "\n".padEnd(indent + 1), false)


fun String.toPath() = Paths.get(this)

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


fun String.matches(regex: String): Boolean = matches(Regex(regex))
