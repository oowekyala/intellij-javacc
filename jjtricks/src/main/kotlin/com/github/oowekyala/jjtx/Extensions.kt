package com.github.oowekyala.jjtx

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun String.splitAroundFirst(delimiter: Char): Pair<String, String> =
    Pair(substringBefore(delimiter), substringAfter(delimiter))

fun String.splitAroundLast(delimiter: Char): Pair<String, String> =
    Pair(substringBeforeLast(delimiter), substringAfterLast(delimiter))

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
