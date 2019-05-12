package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.reporting.ReportingContext
import com.github.oowekyala.jjtx.reporting.TaskCtx
import com.github.oowekyala.jjtx.tasks.GenerateNodesTask
import com.github.oowekyala.jjtx.tasks.GenerateVisitorsTask
import com.github.oowekyala.jjtx.tasks.JjtxTaskKey
import com.github.oowekyala.jjtx.templates.GrammarVBean
import com.github.oowekyala.jjtx.templates.RunVBean
import com.github.oowekyala.jjtx.templates.set
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.isFile
import com.github.oowekyala.jjtx.util.path
import com.intellij.openapi.project.Project
import org.apache.velocity.VelocityContext
import java.nio.file.Path


/**
 * Models the run context of a JJTricks run.
 *
 * @author Clément Fournier
 */
interface JjtxContext {

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
     * introduce a cyclic dependency. After the [GenerateVisitorsTask]
     * is done, the [GenerateNodesTask] uses the completed
     * visitor beans under the key "run", a [RunVBean].
     */
    val globalVelocityContext: VelocityContext


    companion object {


        data class CtxBuilder internal constructor(
            val grammarFile: JccFile,
            var configChain: List<Path> = grammarFile.defaultJjtopts(),
            var io: Io = Io(),
            var messageCollector: MessageCollector = MessageCollector.default(io)
        )

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
                    messageCollector = it.messageCollector
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
internal class JjtxRootContext(
    override val grammarFile: JccFile,
    override val configChain: List<Path>,
    override val messageCollector: MessageCollector,
    override val io: Io
) : JjtxContext {

    override val project: Project = grammarFile.project

    override val grammarName: String = grammarFile.virtualFile.nameWithoutExtension

    override val grammarDir: Path = grammarFile.path.parent


    override val jjtxOptsModel: JjtxOptsModel by lazy {
        // This uses the run context so should only be executed after the constructor returns
        // hence the lazyness
        JjtxOptsModel.parseChain(this, configChain)
    }


    override val globalVelocityContext: VelocityContext by lazy {
        VelocityContext().also {
            it["grammar"] = GrammarVBean.create(this)
            it["global"] = jjtxOptsModel.templateContext
            it["H"] = "#"
        }
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
    SubContext(this, messageCollector.withContext(contextStr))


/**
 * A [subContext] using the given key as a [ReportingContext].
 */
fun JjtxContext.subContext(key: JjtxTaskKey) =
    subContext(TaskCtx(key))


private class SubContext(parent: JjtxContext, subCollector: MessageCollector) : JjtxContext by parent {

    override val messageCollector: MessageCollector = subCollector

}
