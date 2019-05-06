package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.jjtx.templates.GrammarBean
import com.github.oowekyala.jjtx.templates.set
import com.github.oowekyala.jjtx.util.*
import org.apache.velocity.VelocityContext
import java.io.PrintStream
import java.nio.file.Path

/**
 * CLI parameters of a JJTX run.
 *
 *
 * @author Clément Fournier
 */
data class JjtxParams(
    val io: Io,
    val mainGrammarFile: JccFile,
    val outputRoot: Path?,
    val configChain: List<Path>
)
