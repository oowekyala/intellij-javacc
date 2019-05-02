package com.github.oowekyala.jjtx

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * CLI parameters of a JJTX run.
 *
 *
 * @author Clément Fournier
 */
data class JjtxParams(
    val grammarName: String,
    val grammarDir: Path,
    val outputDir: Path,
    val configChain: List<Path> = defaultConfigChain(grammarDir, grammarName)
) {

    val mainGrammarFile: File?
        get() = grammarDir.resolveExisting("$grammarName.jjt")
            ?: grammarDir.resolveExisting("$grammarName.jjtx")


    fun Path.resolveExisting(name: String): File? = resolve(name).takeIf { Files.exists(it) }?.toFile()

    companion object {

        fun parseCliArgs(vararg args: String): JjtxParams? {
            // TODO
            val wdir = Paths.get(System.getProperty("user.dir"))
            return JjtxParams(grammarName = args[0], grammarDir = wdir, outputDir = wdir.resolve("gen"))
        }

        fun defaultConfigChain(grammarDir: Path, grammarName: String) = listOf(
            grammarDir.resolve("$grammarName.jjtopts.yaml")
            // FIXME
            // grammarDir.resolve("$grammarName.jjtopts.json")
        )

    }
}
