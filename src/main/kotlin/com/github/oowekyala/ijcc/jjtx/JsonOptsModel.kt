package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TypeHierarchyTree
import com.google.gson.JsonObject
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author Clément Fournier
 */
class JsonOptsModel(val ctx: JjtxRunContext,
                    override val parentModel: JjtxOptsModel?,
                    json: JsonObject) : JjtxOptsModel {

    private val jjtx: JsonObject = json["jjtx"] as? JsonObject ?: JsonObject()

    override val nodePrefix: String by json.withDefault { "AST" }
    override val nodePackage: String by json.withDefault { "" }

    override val typeHierarchy: TypeHierarchyTree by lazy {
        // lazyness is important, the method calls back to the nodePrefix & nodePackage through the context
        TypeHierarchyTree.fromJson(json["jjtx.typeHierarchy"], ctx)
    }

}

inline fun <reified T> JsonObject.withDefault(crossinline default: () -> T) =
    JsonProperty(this)
        .coerce<T>()
        .map { t -> t ?: default() }

class JsonProperty(private val yamlNode: JsonObject) : ReadOnlyProperty<Any, Any?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Any? = yamlNode["jjtx.${property.name}"]
}


inline fun <reified T> ReadOnlyProperty<Any, *>.coerce(): ReadOnlyProperty<Any, T?> =
    object : ReadOnlyProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? =
            this@coerce.getValue(thisRef, property) as? T
    }


fun <T, R> ReadOnlyProperty<Any, T>.map(f: (T) -> R): ReadOnlyProperty<Any, R> =
    object : ReadOnlyProperty<Any, R> {
        override fun getValue(thisRef: Any, property: KProperty<*>): R =
            f(this@map.getValue(thisRef, property))
    }
