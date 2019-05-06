package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.util.*
import com.intellij.openapi.project.Project
import java.nio.file.Path
import java.nio.file.Paths


/**
 * @param configChain The config chain, in decreasing precedence order
 *
 * @author Clément Fournier
 */
abstract class JjtxContext(val grammarFile: JccFile,
                           val configChain: List<Path>) {

    val project: Project = grammarFile.project

    abstract val io: Io

    abstract val messageCollector: MessageCollector

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

        // they're in decreasing precedence order
        configChain
            .filter { it.isFile() }
            .map { NamedInputStream(it.inputStream(), it.toString()) }
            .plus(RootJjtOpts)
            // but we fold them from least important to most important
            .asReversed()
            .fold<NamedInputStream, JjtxOptsModel>(OldJavaccOptionsModel(grammarFile)) { model, path ->
                try {
                    JjtxOptsModel.parse(this, path, model)
                } catch (e: Exception) {
                    throw RuntimeException("Exception parsing file ${path.filename} : ${e.message}")
                }
            }
    }

}

// TODO there may be some mischief when this is in a jar
val RootJjtOpts: NamedInputStream
    get() = Jjtricks.getResourceAsStream("/jjtx/Root.jjtopts.yaml")!!

val JccFile.path: Path
    get() = Paths.get(virtualFile.path)