package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.reporting.*
import com.github.oowekyala.jjtx.tasks.JjtxTaskKey
import com.github.oowekyala.jjtx.templates.vbeans.GrammarVBean
import com.github.oowekyala.jjtx.templates.vbeans.RunVBean
import com.github.oowekyala.jjtx.util.io.DefaultResourceResolver
import com.github.oowekyala.jjtx.util.io.Io
import com.github.oowekyala.jjtx.util.io.NamedInputStream
import com.github.oowekyala.jjtx.util.io.ResourceResolver
import com.github.oowekyala.jjtx.util.isFile
import com.github.oowekyala.jjtx.util.path
import com.github.oowekyala.jjtx.util.plus
import com.github.oowekyala.jjtx.util.set
import com.intellij.openapi.project.Project
import kotlinx.collections.immutable.toImmutableHashMap
import kotlinx.collections.immutable.toImmutableMap
import org.apache.velocity.VelocityContext
import org.apache.velocity.tools.generic.SortTool
import java.nio.file.Path


/**
 * Models the run context of a JJTricks run.
 *
 * @author Clément Fournier
 */
interface JjtxContext {

    val reportingContext: ReportingContext

    /**
     * Main grammar file.
     */
    val grammarFile: JccFile

    /**
     * Name of the grammar.
     */
    val grammarName: String

    /**
     * Directory of the [grammarFile]
     */
    val grammarDir: Path

    /**
     * Message collector.
     */
    val messageCollector: MessageCollector

    /**
     * IO of the run.
     */
    val io: Io

    /**
     * Project instance, linking this context with the enclosing Intellij platform environment.
     */
    val project: Project

    /**
     * List of jjtopts files generating the [jjtxOptsModel].
     * This list doesn't contain the [JjtxOptsModel.RootJjtOpts] or the grammar file,
     * they're added later.
     */
    val configChain: List<Path>

    /**
     * The fully resolved model taking into account the chained options files.
     * Every model has at its root the options specified in the grammar file,
     * then the default visitor configs and such ([JjtxOptsModel.RootJjtOpts]), then the
     * user-specified chain.
     */
    val jjtxOptsModel: JjtxOptsModel

    /**
     * Velocity context shared by every file generation task.
     * This doesn't contain the visitors, because that would
     * introduce a cyclic dependency. This is used to evaluate
     * the static templates of the options file itself.
     */
    val initialVelocityContext: VelocityContext

    /**
     * This is the root context used by all file gen tasks,
     * it has a [RunVBean] under key "run" to represent the
     * resolved tasks to run.
     */
    val globalVelocityContext: VelocityContext

    val resourceResolver: ResourceResolver<NamedInputStream>


    fun resolveResource(path: String): NamedInputStream? = resourceResolver.getResource(path)


    data class CtxBuilder internal constructor(
        val grammarFile: JccFile,
        var configChain: List<Path> = grammarFile.defaultJjtopts(),
        var io: Io = Io(),
        var resourceResolver: ResourceResolver<NamedInputStream> = DefaultResourceResolver(grammarFile.path.parent),
        var messageCollector: MessageCollector = MessageCollector.default(io)
    )


    companion object {

        /**
         * Build a context for the given grammar file.
         */
        fun buildCtx(grammarFile: JccFile, config: (CtxBuilder) -> Unit = {}): JjtxContext =
            CtxBuilder(grammarFile).also {
                config(it)
            }.let {
                JjtxRootContext(
                    grammarFile = it.grammarFile,
                    configChain = it.configChain,
                    io = it.io,
                    messageCollector = it.messageCollector,
                    resourceResolver = it.resourceResolver
                )
            }


    }
}


/**
 * Root context, built by [JjtxContext.buildCtx]. Subcontexts delegate to their parent.
 *
 * @param configChain The config chain, in decreasing precedence order
 *
 * @author Clément Fournier
 */
private class JjtxRootContext(
    override val grammarFile: JccFile,
    override val configChain: List<Path>,
    override val messageCollector: MessageCollector,
    override val io: Io,
    override val resourceResolver: ResourceResolver<NamedInputStream>
) : JjtxContext {

    override val reportingContext: ReportingContext = RootContext

    override val project: Project = grammarFile.project

    override val grammarName: String = grammarFile.virtualFile.nameWithoutExtension

    override val grammarDir: Path = grammarFile.path.parent


    override val jjtxOptsModel: JjtxOptsModel by lazy {
        // This uses the run context so should only be executed
        // after the constructor returns, hence the lazyness
        JjtxOptsModel.parseChain(this, configChain)
    }


    override val initialVelocityContext: VelocityContext by lazy {
        VelocityContext(jjtxOptsModel.templateContext.toMutableMap()).also {
            it["grammar"] = GrammarVBean.create(this)
            // we need to copy it, otherwise the map ends up containing itself
            // velocity contexts mutate the map they're created with...
            it["global"] = jjtxOptsModel.templateContext.toImmutableHashMap() // alias global context
            // allows escaping the "#" easily.
            it["H"] = "#"
            it["sorter"] = SortTool()
        }
    }

    override val globalVelocityContext: VelocityContext by lazy {
        initialVelocityContext + mapOf(
            "run" to RunVBean.create(this)
        )
    }


}

fun JccFile.defaultJjtopts(): List<Path> {

    val myPath = path
    val grammarName = myPath.fileName

    val opts =
        myPath.resolveSibling("$grammarName.jjtopts").takeIf { it.isFile() }
            ?: myPath.resolveSibling("$grammarName.jjtopts.yaml").takeIf { it.isFile() }

    return listOfNotNull(opts)

}

/**
 * A sub-context, to refine error messages. The [contextStr] is
 * given as information about the current location of the task.
 * The sub-context delegates everything to its parent except error
 * reporting.
 */
fun JjtxContext.subContext(contextStr: ReportingContext): JjtxContext =
    SubContext(this, contextStr)

/**
 * A [subContext] using the given key as a [ReportingContext].
 */
fun JjtxContext.subContext(key: JjtxTaskKey) =
    subContext(taskCtx(key))

/**
 * A sub-context appending the key to the existing subcontext.
 */
fun JjtxContext.subContext(key: String) =
    subContext(reportingContext.subKey(key))


private class SubContext(parent: JjtxContext, override val reportingContext: ReportingContext) : JjtxContext by parent {

    override val messageCollector: MessageCollector = parent.messageCollector.withContext(reportingContext)

}
