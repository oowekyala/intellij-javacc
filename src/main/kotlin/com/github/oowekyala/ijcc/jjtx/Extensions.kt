package com.github.oowekyala.ijcc.jjtx

import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun String.splitAroundFirst(delimiter: Char): Pair<String, String> =
    Pair(substringBefore(delimiter), substringAfter(delimiter))

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
