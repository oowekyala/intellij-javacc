package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.util.Io
import java.nio.file.Path

/**
 * CLI parameters of a JJTX run.
 *
 * @author Clément Fournier
 */
data class JjtxParams(
    /** IO environment. */
    val io: Io,
    /** Main JJT file to process. */
    val mainGrammarFile: JccFile,
    /** Root of the generated files. */
    val outputRoot: Path?,
    /** List of paths indicating the user provided configuration chain, in decreasing precedence order. */
    val configChain: List<Path>
)
