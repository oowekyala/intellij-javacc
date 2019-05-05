package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.Jjtricks.Companion.WRONG_PARAMS_CODE
import com.github.oowekyala.jjtx.util.*
import com.xenomachina.argparser.*
import java.net.URL
import java.nio.file.Path

/**
 * A JJTX run.
 *
 * @author Clément Fournier
 */
class Jjtricks(
    args: ArgParser,
    private val io: Io = Io()
) {


    private val grammarPath: Path by args.positional(
        name = "GRAMMAR",
        help = "Path to a grammar file (the extension can be omitted)"
    ) {
        toPath()
    }.addValidator {
        val myIo = io.copy(exit = { m, c -> throw SystemExitException(m, c) })
        findGrammarFile(myIo, value)
    }


    private val outputRoot: Path by args.storing(
        "-o", "--output",
        help = "Output directory. Files are generated in a package tree rooted in this directory."
    ) {
        toPath()
    }.default(io.wd.resolve("gen")).addValidator {
        if (value.isFile()) {
            throw InvalidArgumentException(
                "-o $value is not a directory"
            )
        }
    }

    private val isDumpConfig by args.flagging(
        "--dump-config",
        help = "Print the fully resolved jjtopts file"
    )
    private val isNoVisitors by args.flagging(
        "--no-visitors",
        help = "Don't generate any visitors"
    )

    private val minSeverity by args.mapping(
        "--quiet" to Severity.FAIL,
        "-q" to Severity.FAIL,
        "--debug" to Severity.INFO,
        "-X" to Severity.INFO,
        help = "Amount of log output to issue"
    ).default {
        Severity.WARN
    }

    private val configFiles: List<Path> by args.adding(
        "-p", "--opts",
        help = "Option files to chain, from lowest to highest priority. The options specified inline in the grammar file always have the lowest priority.",
        argName = "OPTS"
    ) {
        toPath()
    }


    private fun produceContext(): JjtxContext {

        val grammarFile = findGrammarFile(io, grammarPath)
        val configChain = validateConfigFiles(io, grammarFile, configFiles)
        val jccFile = parseGrammarFile(io, grammarFile)

        val params = JjtxParams(
            io = io,
            mainGrammarFile = jccFile,
            outputRoot = outputRoot,
            configChain = configChain
        )

        return JjtxRunContext(params) {
            ErrorCollectorImpl(it, minSeverity)
        }
    }


    fun runTasks() {

        val ctx = produceContext()

        if (!isNoVisitors) {
            GenerateVisitorsTask(outputRoot).execute(ctx)
        }

        if (isDumpConfig) {
            DumpConfigTask(io.stdout).execute(ctx)
        }
    }


    companion object {

        const val WRONG_PARAMS_CODE = -1

        private val DESCRIPTION = """
            A java code generator for JJTree grammars.
        """.trimIndent()

        /**
         * CLI execution.
         */
        @JvmStatic
        fun main(args: Array<String>): Unit = main(Io(), *args)

        @JvmStatic
        fun main(io: Io, vararg args: String): Unit = mainBody(programName = "jjtricks") {

            ArgParser(args, helpFormatter = DefaultHelpFormatter(prologue = DESCRIPTION))
                .parseInto { Jjtricks(io = io, args = it) }
                .runTasks()
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
        if (paths.isEmpty())
            listOf(grammarPath.resolveSibling("$grammarName.jjtopts"))
        else paths

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

        io.stdout.println(message)
        io.exit(WRONG_PARAMS_CODE)
    }
}


private fun Io.bail(message: String): Nothing {
    exit(message, WRONG_PARAMS_CODE)
}

private fun findGrammarFile(io: Io, path: Path): Path {


    val absPath = io.wd.resolve(path)

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

private fun parseGrammarFile(io: Io, file: Path): JccFile {
    val jcc = JjtxLightPsi.parseFile(file.toFile(), JavaccParserDefinition) as? JccFile
        ?: io.bail("File isn't a JJTree file, or internal error")


    (jcc as JccFileImpl).grammarNature = GrammarNature.JJTRICKS

    return jcc
}
