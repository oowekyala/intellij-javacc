package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.splitAroundFirst

/**
 * @author Clément Fournier
 */
data class Namespacer(val namespace: String, val json: AstMap) {

    operator fun get(key: String): DataAstNode? = json.getSplit(fullKey(key))

    infix fun namespace(sub: String) = Namespacer("$namespace.$sub", json)

    private fun fullKey(k: String) = if (namespace.isEmpty()) k else "$namespace.$k"

    private fun AstMap.getSplit(key: String): DataAstNode? {
        when {
            key in this -> return this[key]
            '.' in key  -> {
                val (hd, tl) = key.splitAroundFirst('.')
                val subMap = this[hd] as? AstMap ?: return null
                return subMap.getSplit(tl)
            }
            else        -> return null
        }
    }

}

infix fun AstMap.namespace(ns: String) = Namespacer(ns, this)

