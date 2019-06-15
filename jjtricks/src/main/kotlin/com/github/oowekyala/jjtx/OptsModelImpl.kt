package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.InlineGrammarOptions
import com.github.oowekyala.jjtx.preprocessor.JavaccGenOptions
import com.github.oowekyala.jjtx.preprocessor.JjtreeCompatBean
import com.github.oowekyala.jjtx.preprocessor.completeWith
import com.github.oowekyala.jjtx.preprocessor.toModel
import com.github.oowekyala.jjtx.templates.*
import com.github.oowekyala.jjtx.templates.vbeans.NodeVBean
import com.github.oowekyala.jjtx.typeHierarchy.TypeHierarchyTree
import com.github.oowekyala.jjtx.util.dataAst.*
import com.github.oowekyala.jjtx.util.lazily
import com.github.oowekyala.jjtx.util.map
import com.github.oowekyala.jjtx.util.mapValuesNotNull
import java.util.*
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
    override val isTrackTokens: Boolean by jjtx.withDefault("trackTokens") { parentModel.isTrackTokens }
    override val nodeTakesParserArg: Boolean by jjtx.withDefault { parentModel.nodeTakesParserArg }

    override val grammarName: String by jjtx.withDefault {
        ctx.grammarFile.virtualFile.nameWithoutExtension
    }

    private val javaccBean: JjtreeCompatBean by jjtx.processing("javaccGen") {
        (parentModel as? OptsModelImpl)?.javaccBean?.let { p -> it?.completeWith(p) ?: it ?: p } ?: it ?: JjtreeCompatBean()
    }

    override val javaccGen: JavaccGenOptions by lazy {
        javaccBean.toModel(ctx.subContext("javaccGen.supportFiles"))
    }

    override val templateContext: Map<String, Any> by
    jjtx.withDefault { emptyMap<String, Any>() }
        .map { deepest ->
            // keep all parent keys, but override them
            Collections.unmodifiableMap(parentModel.templateContext + deepest)
        }.lazily()

    private val commonGenExcludes by jjtx.withDefault { emptyList<String>() }


    private val commonGenBeans: Map<String, FileGenBean> by jjtx.processing("commonGen") {
        it.completeWith((parentModel as? OptsModelImpl)?.commonGenBeans.orEmpty(), commonGenExcludes)
    }

    override val commonGen: Map<String, FileGenTask> by lazy {
        commonGenBeans.mapValuesNotNull { (id, v) ->
            v.toFileGen(ctx, positionInfo = null, id = id)?.resolveStaticTemplates(ctx)
        }
    }


    override val typeHierarchy: NodeVBean by jjtx.parsing("typeHierarchy") {
        // laziness is important, the method calls back to the nodePrefix & nodePackage through the context
        val subCtx = ctx.subContext("typeHierarchy")
        val th = TypeHierarchyTree.fromData(it, subCtx).process(subCtx)
        NodeVBean.toBean(th, ctx)
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

