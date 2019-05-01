package com.github.oowekyala.jjtx

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Clément Fournier
 */
data class JjtxParams(
    val grammarName: String,
    val grammarDir: Path,
    val outputDir: Path
) {

    val mainGrammarFile: File?
        get() = grammarDir.resolveExisting("$grammarName.jjt")
            ?: grammarDir.resolveExisting("$grammarName.jjtx")


    val jjtxConfigFile: File?
        get() = grammarDir.resolveExisting("$grammarName.jjtopts.yaml")


    fun Path.resolveExisting(name: String): File? = resolve(name).takeIf { Files.exists(it) }?.toFile()

    companion object {

        fun parse(vararg args: String): JjtxParams? {
            // TODO
            val wdir = Paths.get(System.getProperty("user.dir"))
            return JjtxParams(grammarName = args[0], grammarDir = wdir, outputDir = wdir.resolve("gen"))
        }
    }
}
