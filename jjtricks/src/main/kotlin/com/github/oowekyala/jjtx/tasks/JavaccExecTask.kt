package com.github.oowekyala.jjtx.tasks

import com.github.oowekyala.jjtx.postprocessor.mapJavaccOutput
import com.github.oowekyala.jjtx.reporting.*
import com.github.oowekyala.jjtx.util.exists
import org.javacc.parser.Main
import java.nio.file.Files

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
                otherSources = otherSourceRoots
            )
        }

    }

    companion object {
        /** JavaCC is the least thread-safe program there is... */
        private val mutex = Object()
        private const val JAVACC_ERROR = 1
    }

}
