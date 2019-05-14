package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.jjtx.preprocessor.JavaccGenOptions
import com.github.oowekyala.jjtx.preprocessor.JjtreeCompatBean
import com.github.oowekyala.jjtx.templates.FileGenBean
import com.github.oowekyala.jjtx.templates.GrammarGenerationScheme
import com.github.oowekyala.jjtx.templates.NodeVBean
import com.github.oowekyala.jjtx.templates.toNodeGenerationSchemes
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

    private val javaccGenImpl by jjtx.withDefault<JjtreeCompatBean?>("javaccGen") { null }

    override val javaccGen: JavaccGenOptions by lazy {
        javaccGenImpl?.toModel() ?: parentModel.javaccGen
    }


    override val templateContext: Map<String, Any> by
    jjtx.withDefault { emptyMap<String, Any>() }
        .map { deepest ->
            // keep all parent keys, but override them
            parentModel.templateContext + deepest
        }.lazily()

    override val visitors: Map<String, FileGenBean> by jjtx.withDefault("visitors") {
        emptyMap<String, FileGenBean>()
    }

    private val th: TypeHierarchyTree by JsonProperty(jjtx, "typeHierarchy").map {
        TypeHierarchyTree.fromData(it, ctx)
    }

    /**
     * Type hierarchy after resolution against the grammar, before
     * transformation to [NodeVBean] (which is just a mapping process).
     * This is what's printed by help:dump-config
     */
    internal val resolvedTypeHierarchy: TypeHierarchyTree by lazy {
        th.process(ctx)
    }

    override val typeHierarchy: NodeVBean by lazy {
        // laziness is important, the method calls back to the nodePrefix & nodePackage through the context
        NodeVBean.toBean(resolvedTypeHierarchy, ctx)
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
            it?.load<T>() ?: default()
        }.lazily()


private class JsonProperty(private val namespacer: Namespacer, val name: String? = null) :
    ReadOnlyProperty<Any, DataAstNode?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): DataAstNode? = namespacer[name ?: property.name]
}

