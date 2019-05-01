package com.github.oowekyala.ijcc.jjtx

import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.ijcc.jjtx.util.Namespacer
import com.github.oowekyala.ijcc.jjtx.util.namespace
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author Clément Fournier
 */
class JsonOptsModel(val ctx: JjtxRunContext,
                    override val parentModel: JjtxOptsModel?,
                    json: JsonObject) : JjtxOptsModel {


    private val jjtx: Namespacer = json namespace "jjtx"

    override val nodePrefix: String by jjtx.withDefault { "AST" }
    override val nodePackage: String by jjtx.withDefault { "" }

    override val typeHierarchy: TypeHierarchyTree by lazy {
        // laziness is important, the method calls back to the nodePrefix & nodePackage through the context
        TypeHierarchyTree.fromJson(jjtx["typeHierarchy"], ctx)
    }

}

inline fun <reified T> Namespacer.withDefault(crossinline default: () -> T): ReadOnlyProperty<Any, T> =
    JsonProperty(this)
        .map {
            it?.toJava(T::class.java) as? T ?: default()
        }.lazily()

fun JsonElement.toJava(expectedType: Class<*>): Any? {
    return when (this) {
        is JsonPrimitive -> {
            when {
                isBoolean -> asBoolean
                isString  -> asString
                isNumber  -> {
                    when (expectedType) {
                        Int::class    -> asInt
                        Double::class -> asDouble
                        // etc
                        else          -> asNumber
                    }
                }
                else      -> null
            }
        }
        else             -> null
    }
}


class JsonProperty(private val namespacer: Namespacer) : ReadOnlyProperty<Any, JsonElement?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): JsonElement? = namespacer[property.name]
}

