package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.jjtx.ide.JjtxFullOptionsService
import com.github.oowekyala.jjtx.tasks.DumpConfigTask
import com.github.oowekyala.jjtx.tasks.GenerateVisitorsTask
import com.github.oowekyala.jjtx.util.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.xenomachina.argparser.*
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

    private val severityAndAggregate by args.mapping(
        // turn off warnings
        "--quiet" to (Severity.FAIL to false),
        "-q" to (Severity.FAIL to false),
        // print info messages
        "--debug" to (Severity.INFO to false),
        "-X" to (Severity.INFO to false),
        // stop aggregating warnings
        "--warn" to (Severity.WARNING to false),
        help = "Amount of log output to issue"
    ).default {
        (Severity.WARNING to true)
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

        val (minSeverity, isAggregateEntries) = severityAndAggregate

        return JjtxRunContext(params) {
            MessageCollectorImpl(it, isAggregateEntries, minSeverity)
        }
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
            io.stdout.flush()
            ctx.messageCollector.printReport(io.stderr)
            io.exit(ExitCode.OK)
        }

        if (!isNoVisitors) {
            catchException("Exception while generating visitors") {
                GenerateVisitorsTask(outputRoot).execute(ctx)
                ctx.messageCollector.printReport(io.stderr)
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

            JJTricks is configured via a separate YAML file. If your grammar
            is named "Java.jjt", then your configuration file should be conventionally
            "Java.jjtopts.yaml".

            Available configuration keys:

            ### JJTree keys

            - jjtx.nodePrefix

            - jjtx.nodePackage

            - jjtx.isDefaultVoid

            Those take precedence over the settings in the grammar, but otherwise are
            exact counterparts to their JJTree versions (`NODE_PREFIX`, `NODE_PACKAGE`,
            `DEFAULT_VOID`).

            ### JJTricks visitor generation

            JJTricks uses Apache Velocity templates to generate code.
            A visitor generation task is a run of a template, with
            context variables populated by the engine with information
            about the grammar, and user-provided variables.

            Visitor generation tasks are listed under the map `jjtx.visitors`.
            Each can have the following configuration options:

            - `templateFile`: path to a classpath resource for the velocity template to use.
            - `template`: template written directly in the configuration file. Takes precedence
                over `templateFile` if defined
            - `genClassName`: a template string evaluating to the fully qualified name of the
                visitor class to generate. The context of the visitor is available in the template,
                see below.
            - `formatter`: a key identifying the formatter to use to postprocess the generated file,
                available values are `java` for now, anything else cancels the formatter run
            - `execute`: if "true", the visitor is generated by JJTricks, otherwise it's ignored

            #### Generation context

            A global velocity context is available with the following keys:

            - `grammar`: The [GrammarBean] corresponding to the processed grammar.
            This provides access to the full type hierarchy, among other things.
            - `global`: Namespace for variables shared by all visitors. Those are
            defined in the `jjtx.templateContext` map.
            - Visitor-specific mappings, specified in visitor's [context]
            element, are put directly into the inner context, without namespace.


            ### Context chaining

            Option files can be chained to share common configuration. Each file
            has a parent jjtopts file, to which they delegate the missing keys from
            any incomplete part of the options file.

            By default any jjtopts file delegates directly to the Root.jjtopts.yaml
            file shipped with JJTricks, then to the options specified inline in the
            grammar (in the `options { ... }` section).

            The chain of JJTopts files used by a JJTricks run can be flattened into
            a single file with the `--dump-config` CLI switch, to inspect the actual
            resolved configuration.
        """.trimIndent()

        /**
         * CLI execution.
         */
        @JvmStatic
        fun main(args: Array<String>): Unit = main(Io(), *args)

        @JvmStatic
        fun main(io: Io, vararg args: String): Unit = mainBody(programName = "jjtricks") {

            val jjtx =
                ArgParser(args, helpFormatter = DefaultHelpFormatter(prologue = DESCRIPTION))
                    .parseInto { Jjtricks(io = io, args = it) }

            // environment is open until the end of the CLI run
            JjtxCoreEnvironment.withEnvironment {
                jjtx.doExecute(this)
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


