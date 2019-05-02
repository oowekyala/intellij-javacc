package com.github.oowekyala.jjtx.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.SequenceNode
import org.yaml.snakeyaml.nodes.Node as YamlNode

/**
 * @author Clément Fournier
 */


fun YamlNode.toJson(): JsonElement {


    return when (this) {
        is ScalarNode   -> JsonPrimitive(this.value)
        is SequenceNode -> {
            val arr = JsonArray()
            this.value.map { it.toJson() }.forEach { arr.add(it) }
            arr
        }

        is MappingNode  -> {
            val map = JsonObject()

            for (it in this.value) {
                val k = it.keyNode.let { it as? ScalarNode }?.value
                    ?: run {
                        System.err.println("Key wasn't a string, at ${it.keyNode.startMark}, ignoring mapping")
                        null
                    } ?: continue

                map.add(k, it.valueNode.toJson())
            }

            map
        }
        else            -> throw IllegalStateException("Unknown node type")

    }


}
