package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.ide.JjtxFullOptionsService
import com.github.oowekyala.jjtx.reporting.*
import com.github.oowekyala.jjtx.tasks.*
import com.github.oowekyala.jjtx.tasks.JjtxTaskKey.*
import com.github.oowekyala.jjtx.util.*
import com.github.oowekyala.jjtx.util.io.ExitCode
import com.github.oowekyala.jjtx.util.io.Io
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import java.io.OutputStreamWriter
import java.net.URL
import java.nio.file.Path

/**
 * The CLI of JJTricks.
 *
 * @author Clément Fournier
 */
class Jjtricks(
    private val args: ArgParser,
    private val io: Io = Io()
) {

    private val argCheckIo = io.copy(exit = { m, c -> throw SystemExitException(m, c) })

    private val grammarPath: Path by args.positional(
        name = "GRAMMAR",
        help = "Path to a grammar file (the extension can be omitted)"
    ) {
        toPath().normalize()
    }

    private val tasksImpl: List<List<JjtxTaskKey>> by args.positionalList(
        name = "TASK",
        help = "List of tasks to run, available ones ${values().map { it.ref }}. The syntax 'gen:*' selects all tasks in the 'gen' group"
    ) {
        JjtxTaskKey.parse(this, argCheckIo)
    }.default {
        listOf(JjtxTaskKey.parse("gen:*", argCheckIo))
    }

    private val myTasks: Set<JjtxTaskKey> get() = tasksImpl.flatten().toSet()


    private val outputRoot: Path by args.storing(
        "-o", "--output",
        help = "Output directory. Files are generated in a package tree rooted in this directory."
    ) {
        io.wd.resolve(this).normalize().toAbsolutePath()
    }.default(io.wd.resolve("gen"))
        .addValidator {
        if (value.isFile()) {
            throw SystemExitException(
                "-o $value is not a directory",
                ExitCode.ERROR.toInt
            )
        }
    }

    private val sourceRoots by args.adding(
        "-s", "--source",
        help = "Other source roots. Node files that are already in those roots are not generated."
    ) {
        io.wd.resolve(this).normalize().toAbsolutePath()
    }.default(io.wd.resolve("gen")).addValidator {
        for (d in value) {
            if (d.isFile())
                throw SystemExitException(
                    "-s $d is not a directory",
                    ExitCode.ERROR.toInt
                )
        }
    }

    private val minReportSeverity by args.mapping(
        // turn off warnings
        "--quiet" to Severity.FAIL,
        "-q" to Severity.FAIL,
        // print info messages
        "--debug" to Severity.FINE,
        "-X" to Severity.FINE,
        // stop aggregating warnings
        "--warn" to Severity.WARNING,
        help = "Amount of log output to issue"
    ).default {
        Severity.NORMAL
    }

    private val configFiles: List<Path> by args.adding(
        "-p", "--opts",
        help = "Option files to chain, from highest to lowest priority. The options specified inline in the grammar file always have the lowest priority.",
        argName = "OPTS"
    ) {
        toPath().normalize()
    }

    private fun produceContext(project: Project,
                               rootCollector: MessageCollector,
                               collector: MessageCollector): JjtxContext {

        val collIo = io.copy(exit = { m, _ -> collector.reportFatal(m, null) })

        val grammarFile = findGrammarFile(collIo, grammarPath)
        val configChain = validateConfigFiles(collIo, grammarFile, configFiles, collector)
        val jccFile = parseGrammarFile(collIo, grammarFile, project)


        return JjtxContext.buildCtx(jccFile) {
            it.configChain = configChain
            it.io = io
            it.messageCollector = rootCollector
        }
    }


    fun doExecute(env: JjtxCoreEnvironment) {

        val err = MessageCollector.create(io, minReportSeverity == Severity.NORMAL, minReportSeverity)

        val tasks = myTasks

        // not quiet mode
        if (minReportSeverity < Severity.FAIL) {
            // Don't use the message collector here, to get consistent results
            io.stderr.println("JJTricks v$VERSION")
            io.stderr.println("(Run with -h parameter for a usage summary)")
            if (tasks.isEmpty()) {
                io.stderr.println("No tasks to run!")
                io.exit(ExitCode.OK)
            } else {
                io.stderr.println("Running tasks $tasks")
            }
            io.stderr.println()
        }

        val ctx = err.catchException("Exception while building run context", fatal = true) {
            val init = err.withContext(InitCtx)
            produceContext(env.project, err, init).also {
                init.reportNormal("Config chain: ${it.chainDump}")
            }
        }


        env.registerProjectComponent(GrammarOptionsService::class.java, JjtxFullOptionsService(ctx))


        if (DUMP_CONFIG in tasks) {
            ctx.subContext(DUMP_CONFIG).let {
                it.messageCollector.catchException(null) {
                    DumpConfigTask(it, io.stdout).execute()
                }
            }
        }

        // node generation depends on the visitors
        if (GEN_VISITORS in tasks || GEN_NODES in tasks) {
            ctx.subContext(GEN_VISITORS).let {
                it.messageCollector.catchException(null) {
                    GenerateVisitorsTask(it, outputRoot, sourceRoots.toList()).execute()
                }
            }
        }

        if (GEN_NODES in tasks) {
            ctx.subContext(GEN_NODES).let {
                it.messageCollector.catchException(null) {
                    GenerateNodesTask(it, outputRoot, sourceRoots.toList()).execute()
                }
            }
        }

        if (GEN_JAVACC in tasks) {
            ctx.subContext(GEN_JAVACC).let {
                it.messageCollector.catchException(null) {
                    GenerateJavaccTask(it, outputRoot).execute()
                }
            }
        }

        ctx.messageCollector.concludeReport()
    }

    private fun <T> MessageCollector.catchException(ctxStr: String?, fatal: Boolean = false, block: () -> T): T =
        try {
            block()
        } catch (e: Exception) {
            catchException(null, fatal = true) {
                this.reportException(e, ctxStr, fatal = fatal) as T
            }
        } catch (e: DoExitNowError) {
            io.exit(ExitCode.ERROR)
        }


    companion object {


        const val VERSION = "1.0"

        private val DESCRIPTION = """
            A code generator for JJTree grammars, with emphasis on very high
            flexibility. Can be used as a drop-in replacement for JJTree.

            JJTricks is configured via YAML files outside of the grammar itself.
            If your grammar is named "Java.jjt", then the default configuration file
            should be conventionally "Java.jjtopts.yaml". Additional config files
            may be added with the `--opts` CLI option.

            # CLI behaviour

            * Return code: 0 if everything is alright, 1 if an error occurred

            * IO: standard error stream is used for *every* message from JJTricks.
                Standard output is only used when asked to dump useful info, eg
                with `help:dump-config`

        """.trimIndent()

        private val EXAMPLES = """

            # Examples

            `jjtricks Java`

                Picks up on a Java.jjt file, and Java.jjtopts.yaml if it exists, runs the
                'gen:*' tasks configured in the option file.

            `jjtricks Java help:dump-config`

                Same as above, prints the resolved full configuration file (flattening the
                whole `--opts` chain) to standard output.

        """.trimIndent()

        /**
         * CLI execution, with JVM [Io].
         */
        @JvmStatic
        fun main(args: Array<String>): Unit = main(Io(), *args)

        /**
         * CLI execution with a custom [Io]
         */
        @JvmStatic
        fun main(io: Io, vararg args: String): Unit =
            myMainBody(io = io, programName = "jjtricks") {

                val jjtx =
                    ArgParser(args, helpFormatter = DefaultHelpFormatter(prologue = DESCRIPTION, epilogue = EXAMPLES))
                        .parseInto { Jjtricks(io = io, args = it) }

                // environment is open until the end of the CLI run
                JjtxCoreEnvironment.withEnvironment {
                    jjtx.doExecute(this)
                }

            }

        private fun <R> myMainBody(io: Io, programName: String, body: () -> R): R {
            try {
                return body()
            } catch (e: SystemExitException) {

                val writer = OutputStreamWriter(io.stderr)
                val columns = System.getenv("COLUMNS")?.toInt() ?: 80
                e.printUserMessage(writer, programName, columns)
                writer.flush()

                val ex = when (e.returnCode) {
                    0    -> ExitCode.OK
                    else -> ExitCode.ERROR
                }

                io.exit(ex)
            }
        }


        fun getResource(path: String): URL? = Jjtricks::class.java.getResource(expandResourcePath(path))

        fun getResourceAsStream(path: String): NamedInputStream? =
            Jjtricks::class.java.getResourceAsStream(expandResourcePath(path))
                ?.let {
                    NamedInputStream({ Jjtricks::class.java.getResourceAsStream(expandResourcePath(path)) }, path)
                }

        private fun expandResourcePath(path: String): String {
            return when {
                path.startsWith("/jjtx") -> path.replaceFirst("/jjtx", "/com/github/oowekyala/jjtx")
                else                     -> path
            }
        }
    }
}


private fun validateConfigFiles(io: Io,
                                grammarPath: Path,
                                paths: List<Path>,
                                messageCollector: MessageCollector): List<Path> {

    val grammarName = grammarPath.toFile().nameWithoutExtension

    val (isDefault, defaulted) =
        when {
            paths.isEmpty() -> true to listOf(grammarPath.resolveSibling("$grammarName.jjtopts"))
            else            -> false to paths
        }

    val resolved = defaulted.associateWith { p ->

        val path = io.wd.resolve(p)

        if (path.isFile()) return@associateWith path

        val ext = path.extension
        val fileName = path.fileName

        when (ext) {
            "yaml"    -> path
            "jjtopts" -> path.resolveSibling("$fileName.yaml").takeIf { it.isFile() }
            null      ->
                path.resolveSibling("$fileName.jjtopts").takeIf { it.isFile() }
                    ?: path.resolveSibling("$fileName.jjtopts.yaml").takeIf { it.isFile() }
            else      -> null
        }
    }

    return if (resolved.all { it.value != null })
        resolved.values.mapNotNull { it!! }
    else {
        if (isDefault) {
            messageCollector.reportNormal("No jjtopts file found for grammar $grammarName")
            emptyList()
        } else {
            val message = resolved.filterValues { it == null }.keys.joinToString(
                prefix = "Cannot resolve option files: \n\t",
                separator = "\n\t"
            )
            messageCollector.reportFatal(message)
        }
    }
}

// Used during arg validation, can use IO directly
private fun findGrammarFile(io: Io, path: Path): Path {


    val absPath = io.wd.resolve(path).normalize()

    return if (path.extension == null) {
        val parent = absPath.parent
        val gname = path.fileName.toString()
        val ppath = parent.resolve("$gname.jjtx").takeIf { it.isFile() }
            ?: parent.resolve("$gname.jjt").takeIf { it.isFile() }

        ppath ?: io.bail("Grammar file not found $path.[jjt|jjtx]")
    } else {
        absPath.takeIf { it.isFile() } ?: io.bail("Grammar file not found: $path")
    }
}

private fun parseGrammarFile(io: Io, file: Path, project: Project): JccFile {

    val localFileSystem =
        VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)

    val psiManager = PsiManager.getInstance(project)

    val virtualFile =
        localFileSystem.findFileByPath(file.toAbsolutePath().toString())
            ?: io.bail("Cannot find grammar file in filesystem: $file")

    val jccFile = psiManager.findFile(virtualFile) as? JccFile
        ?: io.bail("File was not a JJTree/JavaCC grammar")

    return jccFile.also {
        (it as JccFileImpl).grammarNature = GrammarNature.JJTRICKS
    }
}


