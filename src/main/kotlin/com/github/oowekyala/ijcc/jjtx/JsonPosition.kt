package com.github.oowekyala.ijcc.jjtx

/**
 * @author Clément Fournier
 */
data class JsonPosition(val path: List<String>) {

    constructor(first: String) : this(listOf(first))

    fun resolve(key: String) = JsonPosition(path + key)

    override fun toString(): String = path.joinToString(" / ") { "\"$it\"" }
}
