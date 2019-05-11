package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.jjtx.preprocessor.JavaccGenOptions
import com.github.oowekyala.jjtx.reporting.MessageCategory.INCOMPLETE_VISITOR_SPEC
import com.github.oowekyala.jjtx.templates.*
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
internal class OptsModelImpl(val ctx: JjtxContext,
                             override val parentModel: JjtxOptsModel,
                             data: AstMap) : JjtxOptsModel {


    private val jjtx: Namespacer = data namespace "jjtx"

    override val inlineBindings: InlineGrammarOptions by lazy {
        parentModel.inlineBindings
    }

    override val nodePrefix: String by jjtx.withDefault { parentModel.nodePrefix }
    override val nodePackage: String by jjtx.withDefault { parentModel.nodePackage }
    override val isDefaultVoid: Boolean by jjtx.withDefault { parentModel.isDefaultVoid }
    override val isTrackTokens: Boolean by jjtx.withDefault { parentModel.isTrackTokens }
    override val javaccGen: JavaccGenOptions by jjtx.withDefault {
        JavaccGenOptions()
    }


    override val templateContext: Map<String, Any> by
    jjtx.withDefault { emptyMap<String, Any>() }
        .map { deepest ->
            // keep all parent keys, but override them
            parentModel.templateContext + deepest
        }.lazily()

    internal val visitorBeans: Map<String, VisitorConfigBean> by jjtx.withDefault<Map<String, VisitorConfigBean>>("visitors") {
        emptyMap()
    }.map { curBeans ->
        curBeans.mapValues { (id, bean) ->
            parentModel.let { it as? OptsModelImpl }?.visitorBeans?.get(id)?.let {
                bean.merge(it)
            } ?: bean
        }
    }.lazily()

    override val visitors: Map<String, VisitorGenerationTask> by lazy {

        val valid = visitorBeans.mapValuesTo(mutableMapOf()) { (id, bean) ->
            try {
                bean.toConfig(id)
            } catch (e: IllegalStateException) {
                // todo don't swallow
                ctx.messageCollector.report(e.message ?: "", INCOMPLETE_VISITOR_SPEC)
                null
            }
        }


        @Suppress("UNCHECKED_CAST")
        valid.filterValues { it != null } as Map<String, VisitorGenerationTask>
    }

    private val th: TypeHierarchyTree by JsonProperty(jjtx, "typeHierarchy").map {
        TypeHierarchyTree.fromData(it, ctx)
    }

    internal val rawTypeHierarchy: TypeHierarchyTree by lazy {
        th.process(ctx)
    }

    override val typeHierarchy: NodeVBean by lazy {
        // laziness is important, the method calls back to the nodePrefix & nodePackage through the context
        NodeVBean.toBean(rawTypeHierarchy, ctx)
    }


    private val ngs: DataAstNode? by JsonProperty(jjtx, "nodeGenerationSchemes")

    private val myGenSchemes: Map<String, GrammarGenerationScheme> by lazy {
        ngs?.toNodeGenerationSchemes(ctx) ?: emptyMap()
    }

    override val grammarGenerationSchemes: Map<String, GrammarGenerationScheme> by lazy {
        // keep all parent keys, but override them
        parentModel.grammarGenerationSchemes + myGenSchemes
    }

    override val activeNodeGenerationScheme: String? by jjtx.withDefault<String?>("activeGenScheme") {
        null
    }

}

private inline fun <reified T> Namespacer.withDefault(propName: String? = null,
                                                      crossinline default: () -> T): ReadOnlyProperty<Any, T> =
    JsonProperty(this, propName)
        .map {
            it?.parse<T>() ?: default()
        }.lazily()


internal inline fun <reified T> DataAstNode.parse(): T {
    val type = object : TypeLiteral<T>() {}
    val any = Gson().fromJson<Any>(toJson(), type.type)
    return any as T
}


private class JsonProperty(private val namespacer: Namespacer, val name: String? = null) :
    ReadOnlyProperty<Any, DataAstNode?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): DataAstNode? = namespacer[name ?: property.name]
}

