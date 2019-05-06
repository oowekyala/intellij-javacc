package com.github.oowekyala.jjtx.samples

import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.util.ParseUtilsMixin
import com.github.oowekyala.jjtx.JjtxParams
import com.github.oowekyala.jjtx.JjtxRunContext
import com.github.oowekyala.jjtx.util.Io
import com.github.oowekyala.jjtx.util.JjtxCoreEnvironment
import com.intellij.openapi.project.Project
import junit.framework.TestCase
import org.junit.runner.RunWith
import org.junit.runner.Runner
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.JUnit4
import org.junit.runners.ParentRunner
import org.junit.runners.Suite
import java.nio.file.Path

/**
 * @author Clément Fournier
 */
abstract class JjtxTestBase : TestCase(), ParseUtilsMixin {

    private val myEnv = JjtxCoreEnvironment.createTestEnvironment()

    override fun getProject(): Project = myEnv.project

    data class CtxBuilder(
        var grammarName: String = "Sample",
        var jccFile: String = JccElementFactory.DummyHeader,
        var opts: OptsSource = JsonOpts("{}"),
        var testGrammarDir: Path = java.nio.file.Files.createTempDirectory("jjtx-test"),
        var testOutputDir: Path = testGrammarDir.resolve("gen")
    )

    fun CtxBuilder.newCtx(): JjtxRunContext {


        val optsFile =
            if (opts is YamlOpts) "$grammarName.jjtopts.yaml"
            else "$grammarName.jjtopts.json"

        val optsPath = testGrammarDir.resolve(optsFile)
        val f = optsPath.toFile()

        f.writeText(opts.str)

        val params = JjtxParams(
            io = Io(),
            mainGrammarFile = jccFile.asJccFile(),
            outputRoot = testOutputDir,
            configChain = listOf(optsPath)
        )

        return JjtxRunContext(
            params
        )
    }


    fun contextBuilder(ctxBuilder: CtxBuilder.() -> Unit): CtxBuilder =
        CtxBuilder().also { it.ctxBuilder() }

    fun buildContext(ctxBuilder: CtxBuilder.() -> Unit): JjtxRunContext =
        CtxBuilder().also { it.ctxBuilder() }.newCtx()
}


sealed class OptsSource {
    abstract val str: String
}

data class YamlOpts(override val str: String) : OptsSource()
data class JsonOpts(override val str: String) : OptsSource()

