package com.github.oowekyala.jjtx.util

/**
 * Namespacer allows structuring config files in a flat
 * or structured format. E.g.
 *
 * jjtx.visitors:
 *
 * is equivalent to
 *
 * jjtx:
 *      visitors:
 *
 *
 *
 * @author Clément Fournier
 */
internal data class Namespacer(val json: AstMap, val namespace: String = "") {

    operator fun get(key: String): DataAstNode? = json.getSplit(fullKey(key))

    infix fun namespace(sub: String) = Namespacer(json, "$namespace.$sub")

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

internal infix fun AstMap.namespace(ns: String) = Namespacer(this, ns)

