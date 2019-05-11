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
import com.github.oowekyala.jjtx.util.NamedInputStream
import com.github.oowekyala.jjtx.util.isFile
import com.github.oowekyala.jjtx.util.path
import com.intellij.openapi.project.Project
import org.apache.velocity.VelocityContext
import java.nio.file.Path


/**
 * Models the run context of a JJTricks run.
 *
 * @param configChain The config chain, in decreasing precedence order
 *
 * @author Clément Fournier
 */
class JjtxContext internal constructor(val grammarFile: JccFile,
                                       val configChain: List<Path>,
                                       val messageCollector: MessageCollector,
                                       val io: Io) {

    val project: Project = grammarFile.project

    val grammarName: String = grammarFile.virtualFile.nameWithoutExtension

    val grammarDir: Path = grammarFile.path.parent

    /**
     * The fully resolved model taking into account the chained options files.
     * Every model has at its root the options specified in the grammar file,
     * then the default visitor configs and such ([RootJjtOpts]), then the
     * user-specified chain.
     */
    val jjtxOptsModel: JjtxOptsModel by lazy {
        // This uses the run context so should only be executed after the constructor returns
        // hence the lazyness
        JjtxOptsModel.parseChain(this, configChain)
    }

    /**
     * Velocity context shared by every file generation task.
     * This doesn't contain the visitors, because that would
     * introduce a cyclic dependency. After the [GenerateVisitorsTask]
     * is done, the [GenerateNodesTask] uses the completed
     * visitor beans under the key "run", a [RunVBean].
     */
    val globalVelocityContext: VelocityContext by lazy {
        VelocityContext().also {
            it["grammar"] = GrammarVBean.create(this)
            it["global"] = jjtxOptsModel.templateContext
            it["H"] = "#"
        }
    }

    /**
     * A sub-context, to refine error messages. The [contextStr] is
     * given as information about the current location of the task.
     */
    fun subContext(contextStr: ReportingContext): JjtxContext = buildCtx(grammarFile) {
        it.io = io
        it.configChain = configChain
        it.messageCollector = messageCollector.withContext(contextStr)
    }


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
                JjtxContext(
                    grammarFile = it.grammarFile,
                    configChain = it.configChain,
                    io = it.io,
                    messageCollector = it.messageCollector
                )
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

fun JjtxContext.subContext(key: JjtxTaskKey) = subContext(TaskCtx(key))

// TODO there may be some mischief when this is in a jar
internal val RootJjtOpts: NamedInputStream
    get() = Jjtricks.getResourceAsStream("/jjtx/Root.jjtopts.yaml")!!

