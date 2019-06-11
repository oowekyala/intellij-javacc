package com.github.oowekyala

import com.google.gson.GsonBuilder
import org.yaml.snakeyaml.Yaml
import java.io.File


fun yamlToJson(i: File, o: File) {

    val map = i.bufferedReader().use {
        Yaml().load<Map<String, Any>>(it)
    }

    o.bufferedWriter().use { w ->
        GsonBuilder().setPrettyPrinting().create().run { this.toJson(map, w) }
    }

}
