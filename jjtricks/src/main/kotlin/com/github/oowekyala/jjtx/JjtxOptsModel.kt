package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.IGrammarOptions
import com.github.oowekyala.jjtx.preprocessor.JavaccGenOptions
import com.github.oowekyala.jjtx.reporting.JjtricksExceptionWrapper
import com.github.oowekyala.jjtx.reporting.reportWrappedException
import com.github.oowekyala.jjtx.reporting.subKey
import com.github.oowekyala.jjtx.templates.FileGenTask
import com.github.oowekyala.jjtx.templates.GrammarGenerationScheme
import com.github.oowekyala.jjtx.templates.vbeans.NodeVBean
import com.github.oowekyala.jjtx.util.dataAst.AstMap
import com.github.oowekyala.jjtx.util.dataAst.parseAndResolveIncludes
import com.github.oowekyala.jjtx.util.dataAst.validateJjtopts
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.github.oowekyala.jjtx.util.io.namedInputStream
import com.github.oowekyala.jjtx.util.isFile
import com.github.oowekyala.jjtx.util.toPath
import java.nio.file.Path

/**
 * Models a jjtopts configuration file.
 *
 * @author Clément Fournier
 */
interface JjtxOptsModel : IGrammarOptions {

    /**
     * The parent model in the chain. Properties that
     * are not found in this model are delegated to the parent.
     */
    val parentModel: JjtxOptsModel?

    override val nodePrefix: String

    override val nodePackage: String

    override val isDefaultVoid: Boolean

    /**
     * The fully resolved type hierarchy tree,
     * not inherited.
     */
    val typeHierarchy: NodeVBean

    /**
     * Global template variables, merged with the parent maps.
     */
    val templateContext: Map<String, Any>

    /**
     * Map of ids to runnable visitor generation tasks.
     */
    val commonGen: Map<String, FileGenTask>

    /**
     * The node generation scheme, not merged if provided.
     */
    val nodeGen: GrammarGenerationScheme?


    /**
     * Generation options for the JavaCC file.
     */
    val javaccGen: JavaccGenOptions


    companion object {

        const val DefaultRootNodeName = "Node"


        // TODO there may be some mischief when this is in a jar
        internal val RootJjtOpts: NamedInputStream
            get() = Jjtricks.getResourceAsStream("/jjtx/Root.jjtopts.yaml")!!


        /**
         * Parses and chains a full chain of paths, throwing an
         * exception if one model can't be parsed. The context
         * must be initialised (constructor must have completed).
         *
         * @param ctx Context
         * @param configChain In decreasing precedence order. The default root file
         *                    and the inline options will be added to the top of the chain.
         *
         */
        fun parseChain(ctx: JjtxContext, configChain: List<Path>): JjtxOptsModel =
            // they're in decreasing precedence order
            configChain
                .filter { it.isFile() }
                .map { it.namedInputStream() }
                .plus(RootJjtOpts)
                // but we fold them from least important to most important
                .asReversed()
                .fold<NamedInputStream, JjtxOptsModel>(OldJavaccOptionsModel(ctx.grammarFile)) { model, path ->
                    try {
                        parse(ctx, path, model)
                    } catch (e: Exception) {
                        val wrapper = JjtricksExceptionWrapper.withKnownFileCtx(
                            e,
                            path.newInputStream().bufferedReader().readText(),
                            path.filename.toPath()
                        )

                        ctx.messageCollector.reportWrappedException(
                            wrapper,
                            contextStr = "Parsing options file",
                            fatal = true
                        )
                        throw AssertionError("Shouldn't happen")
                    }
                }

        /**
         * Parse a single jjtopts [file] into a [JjtxOptsModel].
         * Supported models are JSON and Yaml. Yaml is much more
         * convenient, if only because it allows inputting multiline
         * strings.
         *
         * @throws Exception on parsing errors.
         */
        fun parse(ctx: JjtxContext,
                  file: NamedInputStream,
                  parent: JjtxOptsModel): JjtxOptsModel =
            file.parseAndResolveIncludes(ctx.resourceResolver, ctx.messageCollector)
                .apply { validateJjtopts(ctx.subContext(ctx.reportingContext.subKey("validation"))) }
                .let { OptsModelImpl(ctx, parent, it as AstMap) }


    }

}
