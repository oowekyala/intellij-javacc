package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.splitAroundFirst
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * @author Clément Fournier
 */
data class Namespacer(val namespace: String, val json: JsonObject) {

    operator fun get(key: String): JsonElement? = json.getSplit("$namespace.$key")

    infix fun namespace(sub: String) = Namespacer("$namespace.$sub", json)
}

infix fun JsonObject.namespace(ns: String) = Namespacer(ns, this)


fun JsonObject.getSplit(key: String): JsonElement? {
    when {
        has(key)   -> return this[key]
        '.' in key -> {
            val (hd, tl) = key.splitAroundFirst('.')
            val subMap = this[hd] as? JsonObject ?: return null
            return subMap.getSplit(tl)
        }
        else       -> return null
    }
}
