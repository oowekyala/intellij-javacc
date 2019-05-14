package com.github.oowekyala.jjtx.util.dataAst

import com.github.oowekyala.ijcc.util.tail
import com.github.oowekyala.jjtx.util.JsonPointer

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
internal class Namespacer(val data: DataAstNode, val namespace: String = "", val delim: Char = '.') {

    operator fun get(key: String): DataAstNode? = data.findPointer(fullKey(key).split(delim))

    infix fun namespace(sub: String) = Namespacer(data, fullKey(sub), delim = delim)

    private fun fullKey(k: String) = if (namespace.isEmpty()) k else "$namespace$delim$k"

    override fun toString(): String = data.prettyPrint()

}

fun DataAstNode.findPointer(jsonPointer: JsonPointer) = findPointer(path = jsonPointer.path)

fun DataAstNode.findPointer(vararg path: String): DataAstNode? = findPointer(path.toList())

fun DataAstNode.findPointer(path: List<String>): DataAstNode? {
    if (path.isEmpty()) return this

    val hd = path[0]
    val tl = path.tail()

    return when (this) {
        is AstSeq -> hd.toIntOrNull()?.let { this.getOrNull(it) }?.findPointer(tl)
        is AstMap -> when (hd) {
            in this -> this[hd]?.findPointer(tl)
            else    -> null
        }
        else      -> null
    }
}

internal infix fun AstMap.namespace(ns: String) = Namespacer(this, ns)

