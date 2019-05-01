package com.github.oowekyala.jjtx

import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.Namespacer
import com.github.oowekyala.jjtx.util.namespace
import com.github.oowekyala.jjtx.templates.VisitorConfig
import com.google.gson.*
import org.apache.commons.lang3.reflect.TypeLiteral
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

    override val visitors: List<VisitorConfig> by jjtx.withDefault {
        emptyList<VisitorConfig>()
    }

    private val th: TypeHierarchyTree by JsonProperty(jjtx, "typeHierarchy").map {
        TypeHierarchyTree.fromJson(it, ctx)
    }

    override val typeHierarchy: TypeHierarchyTree by lazy {
        // laziness is important, the method calls back to the nodePrefix & nodePackage through the context
        th.process(ctx)
    }

}

inline fun <reified T> Namespacer.withDefault(crossinline default: () -> T): ReadOnlyProperty<Any, T> =
    JsonProperty(this)
        .map {
            it?.let {
                val type = object : TypeLiteral<T>() {}
                val any = Gson().fromJson<Any>(it, type.type)
                any as T
            }
                ?: default()
        }.lazily()


fun JsonElement.toJava(expectedType: Class<*>): Any? {
    return when (this) {
        is JsonArray     -> when (expectedType) {
            List::class.java -> (this as JsonArray).toList()
            else             -> (this as JsonArray).toList()
        }
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


class JsonProperty(private val namespacer: Namespacer, val name: String? = null) : ReadOnlyProperty<Any, JsonElement?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): JsonElement? = namespacer[name ?: property.name]
}

