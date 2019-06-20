package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.JjtxContext
import com.github.oowekyala.jjtx.postprocessor.mapJavaccOutput
import com.github.oowekyala.jjtx.reporting.*
import com.github.oowekyala.jjtx.util.asQnamePath
import com.github.oowekyala.jjtx.util.exists
import org.javacc.parser.Main
import java.nio.file.Files
import java.nio.file.Path

class JavaccExecTask(private val ctx: TaskCtx) : JjtxTask() {


    override fun execute() {

        with(ctx) {
            val jj = ctx.genJjPath(outputDir)

            if (!jj.exists()) {
                ctx.messageCollector.reportNonFatal("JavaCC grammar not found: $jj\nCan't execute JavaCC")
                return
            }

            val tmpOutput = Files.createTempDirectory("javacc-from-jjtricks")

            val jccExitCode = synchronized(mutex) {
                try {
                    // TODO redirect IO

                    Main.mainProgram(
                        arrayOf(
                            "-OUTPUT_DIRECTORY=$tmpOutput",
                            "-STATIC=false",
                            "-USER_CHAR_STREAM=true",
                            jj.toString()
                        )
                    )


                } catch (t: Throwable) {
                    ctx.messageCollector.reportFatalException(t, "Executing JavaCC")
                }
            }


            if (jccExitCode == JAVACC_ERROR) {
                ctx.messageCollector.reportFatal("JavaCC exited with abnormal status code ($JAVACC_ERROR)")
            }

            ctx.messageCollector.report("Ran JavaCC in $tmpOutput",
                MessageCategory.DEBUG
            )

            mapJavaccOutput(
                ctx = ctx,
                jccOutput = tmpOutput,
                realOutput = outputDir,
                outputFilter = outputFilter(ctx, tmpOutput, otherSourceRoots, overwriteReal = false)
            )
        }

    }

    companion object {
        /** JavaCC is the least thread-safe program there is... */
        private val mutex = Object()
        private const val JAVACC_ERROR = 1
    }

}

/**
 * Selects qualified names whose files should be written out.
 *
 * TODO also look into a user-provided classpath
 */
fun outputFilter(
    ctx: JjtxContext,
    realOutput: Path,
    otherSourceRoots: List<Path>,
    overwriteReal: Boolean = true
): (String) -> Boolean =
    fun(qname: String): Boolean {

        fun noGenMessage(qname: String, realOutput: Path) =
            "Class $qname was not generated because present in $realOutput"


        val rel = qname.asQnamePath()

        for (root in otherSourceRoots) {
            if (root.resolve(rel).exists()) {
                ctx.messageCollector.report(
                    noGenMessage(qname, root),
                    MessageCategory.CLASS_NOT_GENERATED
                )
                return false
            }
        }

        if (realOutput.resolve(rel).exists()) {
            val message =
                if (overwriteReal) "Class $qname will be overwritten in $realOutput"
                else noGenMessage(qname, realOutput)

            ctx.messageCollector.debug(message)
            return overwriteReal
        }

        return true
    }

