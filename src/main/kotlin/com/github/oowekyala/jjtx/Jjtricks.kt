package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.JavaccParserDefinition
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.Jjtricks.Companion.WRONG_PARAMS_CODE
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.NamedInputStream
import com.github.oowekyala.jjtx.util.extension
import com.intellij.util.io.isFile
import kotlinx.cli.*
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * A JJTX run.
 *
 * @author Clément Fournier
 */
class Jjtricks(
    val io: Io
) {

    private val cli = CommandLineInterface(
        commandName = "jjtricks",
        description = "A preprocessor for JJTree files and code generator.",
        defaultHelpPrinter = io.helpPrinter()
    )

    val isDumpConfig by cli.flagArgument("show-config", "Print the fully resolved jjtopts file")
    val isGenerateVisitors by cli.flagArgument("visitors", "Generate the visitors")


    private val grammarPath: Path? by cli.positionalArgument(
        "<grammarFile>",
        "Path to a grammar file (the extension can be omitted)",
        minArgs = 1
    ) { Paths.get(it) }

    private val configFiles: List<Path> by cli.positionalArgumentsList(
        name = "-c",
        help = """Config file to use, from lowest to highest priority. The options specified inline in the grammar file always have the lowest priority.""".trimMargin()
    ) { str ->
        Paths.get(str)
    }

    private val outputRoot: Path? by cli.flagValueArgument(
        flags = listOf("-o", "--output"),
        valueSyntax = "<dir>",
        help = "Root of the output directory. Files are generated in a package tree rooted in this directory.",
        initialValue = grammarPath?.resolveSibling("generated")
    ) {
        Paths.get(it)
    }

    fun printHelpAndExit(message: String, code: Int = WRONG_PARAMS_CODE): Nothing {
        cli.defaultHelpPrinter!!.printText(message)
        cli.defaultHelpPrinter!!.printText("(Run with -h parameter to show help)")
        io.exit(code)
    }


    // TODO help, overwrite files,


    fun produceContext(args: Array<String>): JjtxContext {

        try {
            cli.parse(args)
        } catch (e: Exception) {
            exitProcess(1)
        }

        val g = grammarPath ?: run {
            cli.printHelp()
            io.exit(WRONG_PARAMS_CODE)
        }

        val grammarFile = findGrammarFile(io, g)
        val configChain = validateConfigFiles(io, grammarFile, configFiles)
        val jccFile = parseGrammarFile(io, grammarFile)

        val params = JjtxParams(
            io = io,
            mainGrammarFile = jccFile,
            outputRoot = outputRoot,
            configChain = configChain
        )

        return JjtxRunContext(params)
    }


    fun runTasks(ctx: JjtxContext) {

        if (isGenerateVisitors) {

            val o = outputRoot ?: let {

                io.stdout.println("No --output dir specified, defaulting to ./gen")

                val dir = io.wd.resolve("gen")

                if (dir.isFile()) {
                    io.stdout.println("Expected a directory, but was a file: $dir")
                    io.exit(WRONG_PARAMS_CODE)
                }

                dir
            }

            GenerateVisitorsTask(o).execute(ctx)
        }

        if (isDumpConfig) {
            DumpConfigTask.execute(ctx)
        }
    }


    companion object {

        const val WRONG_PARAMS_CODE = -1

        /**
         * CLI execution.
         */
        @JvmStatic
        fun main(args: Array<String>) {

            val jjtx = Jjtricks(Io())
            val ctx = jjtx.produceContext(args)
            jjtx.runTasks(ctx)
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

    val resolved = defaulted.associateWith {

        val path = io.wd.resolve(it)

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
    stdout.println(message)
    exit(WRONG_PARAMS_CODE)
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
