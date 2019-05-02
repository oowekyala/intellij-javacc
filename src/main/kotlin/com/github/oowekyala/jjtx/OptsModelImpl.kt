package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.jjtx.templates.VisitorConfig
import com.github.oowekyala.jjtx.templates.VisitorConfigBean
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.*
import com.google.gson.Gson
import org.apache.commons.lang3.reflect.TypeLiteral
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A chain of json config files ending up in the inline bindings.
 *
 * @author Clément Fournier
 */
class OptsModelImpl(val ctx: JjtxContext,
                    override val parentModel: JjtxOptsModel,
                    json: AstMap) : JjtxOptsModel {


    private val jjtx: Namespacer = json namespace "jjtx"

    override val inlineBindings: InlineGrammarOptions by lazy {
        generateSequence(parentModel) { it.parentModel }.filterIsInstance<InlineGrammarOptions>().first()
    }

    override val nodePrefix: String by jjtx.withDefault { parentModel.nodePrefix }
    override val nodePackage: String by jjtx.withDefault { parentModel.nodePackage }
    override val isDefaultVoid: Boolean by jjtx.withDefault { parentModel.isDefaultVoid }

    override val templateContext: Map<String, Any> by
    jjtx.withDefault { emptyMap<String, Any>() }
        .map { deepest ->
            // keep all parent keys, but override them
            parentModel.templateContext + deepest
        }.lazily()

    override val visitors: List<VisitorConfig> by jjtx.withDefault<List<VisitorConfigBean>> {
        emptyList()
    }.map {
        it.map { it.toConfig() }
    }.lazily()

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
                val any = Gson().fromJson<Any>(it.toJson(), type.type)
                any as T
            }
                ?: default()
        }.lazily()


class JsonProperty(private val namespacer: Namespacer, val name: String? = null) : ReadOnlyProperty<Any, DataAstNode?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): DataAstNode? = namespacer[name ?: property.name]
}

