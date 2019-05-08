package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.templates.GrammarBean
import com.github.oowekyala.jjtx.templates.set
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.NamedInputStream
import com.github.oowekyala.jjtx.util.isFile
import com.intellij.openapi.project.Project
import org.apache.velocity.VelocityContext
import java.nio.file.Path
import java.nio.file.Paths


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

    internal val grammarBean: GrammarBean by lazy {
        GrammarBean.create(this)
    }

    val globalVelocityContext: VelocityContext by lazy {
        VelocityContext().also {
            it["grammar"] = grammarBean
            it["global"] = jjtxOptsModel.templateContext
            it["H"] = "#"
        }
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

// TODO there may be some mischief when this is in a jar
val RootJjtOpts: NamedInputStream
    get() = Jjtricks.getResourceAsStream("/jjtx/Root.jjtopts.yaml")!!

val JccFile.path: Path
    get() = Paths.get(virtualFile.path).normalize()
