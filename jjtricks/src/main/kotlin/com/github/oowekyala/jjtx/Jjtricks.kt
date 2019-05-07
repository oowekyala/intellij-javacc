package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.ide.JjtxFullOptionsService
import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.reporting.Severity
import com.github.oowekyala.jjtx.tasks.DumpConfigTask
import com.github.oowekyala.jjtx.tasks.GenerateVisitorsTask
import com.github.oowekyala.jjtx.util.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.xenomachina.argparser.*
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

    private val grammarPath: Path by args.positional(
        name = "GRAMMAR",
        help = "Path to a grammar file (the extension can be omitted)"
    ) {
        toPath().normalize()
    }.addValidator {
        val myIo = io.copy(exit = { m, c -> throw SystemExitException(m, c) })
        findGrammarFile(myIo, value)
    }


    private val outputRoot: Path by args.storing(
        "-o", "--output",
        help = "Output directory. Files are generated in a package tree rooted in this directory."
    ) {
        toPath().normalize()
    }.default(io.wd.resolve("gen")).addValidator {
        if (value.isFile()) {
            throw InvalidArgumentException(
                "-o $value is not a directory"
            )
        }
    }

    private val isDumpConfig by args.flagging(
        "--dump-config",
        help = "Print the fully resolved jjtopts file and exits"
    )
    private val isNoVisitors by args.flagging(
        "--no-visitors",
        help = "Don't generate any visitors"
    )

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


    private fun produceContext(project: Project): JjtxRunContext {

        args.force()

        val grammarFile = findGrammarFile(io, grammarPath)
        val configChain = validateConfigFiles(io, grammarFile, configFiles)
        val jccFile = parseGrammarFile(io, grammarFile, project)

        val params = JjtxParams(
            io = io,
            mainGrammarFile = jccFile,
            outputRoot = outputRoot,
            configChain = configChain
        )

        val collector = MessageCollector.create(io, minReportSeverity == Severity.NORMAL, minReportSeverity)

        return JjtxRunContext(params, collector)
    }


    fun doExecute(env: JjtxCoreEnvironment) {

        val ctx = catchException("Exception while building run context") {
            produceContext(env.project)
        }

        env.registerProjectComponent(GrammarOptionsService::class.java, JjtxFullOptionsService(ctx))

//        env.applicationEnvironment.registerApplicationService(GrammarOptionsService::class.java, JjtxFullOptionsService(ctx))

        if (isDumpConfig) {
            catchException("Exception while dumping configuration task") {
                DumpConfigTask(io.stdout).execute(ctx)
            }
            ctx.messageCollector.concludeReport()
            io.exit(ExitCode.OK)
        }

        if (!isNoVisitors) {
            catchException("Exception while generating visitors") {
                GenerateVisitorsTask(outputRoot).execute(ctx)
                ctx.messageCollector.concludeReport()
            }
        }

    }

    private fun <T> catchException(errorCase: String, block: () -> T): T =
        try {
            block()
        } catch (e: Exception) {
            io.bail(e.message ?: errorCase)
        }


    companion object {

        private val DESCRIPTION = """
            A code generator for JJTree grammars, with emphasis on very high
            flexibility. Intended as a replacement for JJTree's inflexible code
            generator.

            JJTricks is configured via YAML files outside the grammar themselves.
            If your grammar is named "Java.jjt", then the default configuration file
            should be conventionally "Java.jjtopts.yaml". Additional config files
            may be added with the `--opts` CLI option.

            # CLI behaviour

            * Return code: 0 if everything is alright, 1 if an error occurred
            * IO: standard error stream is used for *every* message from JJTricks.
                Standard output is only used when asked to dump useful info, eg
                with `--dump-config`

        """.trimIndent()

        private val EXAMPLES = """

            # Examples

            `jjtricks Java`

                Picks up on a Java.jjt file, and Java.jjtopts.yaml if it exists, runs the
                visitors declared for execution in the options file.

            `jjtricks Java --dump-config`

                Same as above, prints the resolved full configuration file (flattening the
                whole `--opts` chain) to standard output.

        """.trimIndent()

        /**
         * CLI execution.
         */
        @JvmStatic
        fun main(args: Array<String>): Unit = main(Io(), *args)

        @JvmStatic
        fun main(io: Io, vararg args: String): Unit = mainBody(io = io, programName = "jjtricks") {

            val jjtx =
                ArgParser(args, helpFormatter = DefaultHelpFormatter(prologue = DESCRIPTION, epilogue = EXAMPLES))
                    .parseInto { Jjtricks(io = io, args = it) }

            // environment is open until the end of the CLI run
            JjtxCoreEnvironment.withEnvironment {
                jjtx.doExecute(this)
            }

        }

        private fun <R> mainBody(io: Io, programName: String, body: () -> R): R {
            try {
                return body()
            } catch (e: SystemExitException) {

                val writer = OutputStreamWriter(if (e.returnCode == 0) io.stdout else io.stderr)
                val columns = System.getenv("COLUMNS")?.toInt() ?: 80
                e.printUserMessage(writer, programName, columns)

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
                    NamedInputStream(it, path)
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
                                paths: List<Path>): List<Path> {

    val grammarName = grammarPath.toFile().nameWithoutExtension

    val defaulted =
        when {
            paths.isEmpty() -> listOf(grammarPath.resolveSibling("$grammarName.jjtopts"))
            else            -> paths
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

        val message = resolved.filterValues { it == null }.keys.joinToString(
            prefix = "Cannot resolve option files: \n\t",
            separator = "\n\t"
        )
        io.bail(message)
    }
}


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
            ?: io.bail("Cannot find file in filesystem: $file")

    val jccFile = psiManager.findFile(virtualFile) as? JccFile
        ?: io.bail("Find was not a JJTree/JavaCC grammar")

    return jccFile.also {
        (it as JccFileImpl).grammarNature = GrammarNature.JJTRICKS
    }
}


