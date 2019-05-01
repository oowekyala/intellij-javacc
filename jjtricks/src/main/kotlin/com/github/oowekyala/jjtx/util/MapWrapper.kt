package com.github.oowekyala.jjtx.util

import com.github.oowekyala.jjtx.splitAroundFirst
import org.yaml.snakeyaml.Yaml
import java.io.Reader

sealed class ConfigModel


data class Node(val any: String) : ConfigModel()

data class Seq(val list: List<ConfigModel>)

object NullModel


/**
 * Model of a yaml node / json node.
 *
 * @author Clément Fournier
 */
data class MapWrapper(val myMap: Map<String, Any?>) : ConfigModel() {


    override fun toString(): String = "Node$myMap"


    operator fun get(key: String): Any? = myMap[key]

    fun getRecursive(key: String): Any? =
        if ('.' in key) {
            val (before, after) = key.splitAroundFirst('.')

            val subnode = get(before)
            if (subnode is MapWrapper) subnode.getRecursive(after) else null
        } else get(key)

    val size: Int
        get() = myMap.size

    val keys: Set<String>
        get() = myMap.keys

    companion object {


        fun parseYaml(reader: Reader): MapWrapper {
            val yaml: Any = Yaml().load(reader)
            return if (yaml is Map<*, *>)
                create(yaml)
            else empty()
        }

        fun empty() = create(emptyMap<Any, Any>())

        private fun Map<*, *>.selectKeys(): Map<String, Any?> {
            val myMap = mutableMapOf<String, Any?>()
            for ((k, v) in this) {
                if (k is String && v is Map<*, *>) myMap[k] = create(v)
                else if (k is String) myMap[k] = v
            }
            return myMap
        }

        fun create(m: Map<*, *>) = MapWrapper(m.selectKeys())

    }
}
