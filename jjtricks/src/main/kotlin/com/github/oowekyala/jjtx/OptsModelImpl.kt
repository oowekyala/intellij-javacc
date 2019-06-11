package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.jjtx.preprocessor.JavaccGenOptions
import com.github.oowekyala.jjtx.preprocessor.JjtreeCompatBean
import com.github.oowekyala.jjtx.templates.*
import com.github.oowekyala.jjtx.templates.vbeans.NodeVBean
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.dataAst.*
import com.github.oowekyala.jjtx.util.lazily
import com.github.oowekyala.jjtx.util.map
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A chain of json/yaml config files ending up in the inline bindings.
 *
 * @author Clément Fournier
 */
internal class OptsModelImpl(rootCtx: JjtxContext,
                             override val parentModel: JjtxOptsModel,
                             data: AstMap) : JjtxOptsModel {

    private val ctx = rootCtx.subContext("optsParsing")

    private val jjtx: Namespacer = data namespace "jjtx"

    override val inlineBindings: InlineGrammarOptions by lazy {
        parentModel.inlineBindings
    }

    override val nodePrefix: String by jjtx.withDefault { parentModel.nodePrefix }
    override val nodePackage: String by jjtx.withDefault { parentModel.nodePackage }
    override val isDefaultVoid: Boolean by jjtx.withDefault { parentModel.isDefaultVoid }
    override val isTrackTokens: Boolean by jjtx.withDefault { parentModel.isTrackTokens }

    override val javaccGen: JavaccGenOptions by jjtx.withDefault<JjtreeCompatBean?>("javaccGen") {
        null
    }.map {
        it?.toModel() ?: parentModel.javaccGen
    }.lazily()


    override val templateContext: Map<String, Any> by
    jjtx.withDefault { emptyMap<String, Any>() }
        .map { deepest ->
            // keep all parent keys, but override them
            parentModel.templateContext + deepest
        }.lazily()

    private val commonGenExcludes by jjtx.withDefault { emptyList<String>() }


    internal val commonGenBeans: Map<String, FileGenBean> by jjtx.processing("commonGen") {
        val parentBeans = (parentModel as? OptsModelImpl)?.commonGenBeans.orEmpty()
        parentBeans + (it.orEmpty() - commonGenExcludes).mapValues { (id, bean) ->
            parentBeans[id]?.let { bean.completeWith(it) } ?: bean
        }
    }

    override val commonGen: Map<String, FileGenTask> by lazy {
        commonGenBeans.mapValues { (id, v) ->
            v.toFileGen(ctx, positionInfo = null, id = id)?.resolveStaticTemplates(ctx)
        }
            .filterValues { it != null }
            as Map<String, FileGenTask>
    }


    /**
     * Type hierarchy after resolution against the grammar, before
     * transformation to [NodeVBean] (which is just a mapping process).
     * This is what's printed by help:dump-config
     */
    internal val resolvedTypeHierarchy: TypeHierarchyTree by jjtx.parsing("typeHierarchy") {
        val subCtx = ctx.subContext("typeHierarchy")
        TypeHierarchyTree.fromData(it, subCtx).process(subCtx)
    }

    override val typeHierarchy: NodeVBean by lazy {
        // laziness is important, the method calls back to the nodePrefix & nodePackage through the context
        NodeVBean.toBean(resolvedTypeHierarchy, ctx)
    }

    override val nodeGen: GrammarGenerationScheme? by jjtx.parsing {
        // keep all parent keys, but override them
        it?.toNodeGenerationScheme(ctx.subContext("nodeGen")) ?: parentModel.nodeGen
    }

}

private inline fun <reified T> Namespacer.withDefault(propName: String? = null,
                                                      crossinline default: () -> T): ReadOnlyProperty<Any, T> =
    JsonProperty(this, propName)
        .map {
            it?.load<T>() ?: default()
        }.lazily()

private inline fun <reified T> Namespacer.processing(propName: String? = null,
                                                     crossinline mapper: (T?) -> T): ReadOnlyProperty<Any, T> =
    JsonProperty(this, propName)
        .map {
            mapper(it?.load<T>())
        }.lazily()

private inline fun <reified T> Namespacer.parsing(propName: String? = null,
                                                  crossinline mapper: (DataAstNode?) -> T): ReadOnlyProperty<Any, T> =
    JsonProperty(this, propName).map { mapper(it) }.lazily()


private class JsonProperty(private val namespacer: Namespacer, val name: String? = null) :
    ReadOnlyProperty<Any, DataAstNode?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): DataAstNode? = namespacer[name ?: property.name]
}

