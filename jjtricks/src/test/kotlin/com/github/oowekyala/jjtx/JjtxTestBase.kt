package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.JjtreeFileType
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccElementFactory
import com.github.oowekyala.ijcc.lang.util.ParseUtilsMixin
import com.github.oowekyala.jjtx.ide.JjtxFullOptionsService
import com.github.oowekyala.jjtx.reporting.MessageCollector
import com.github.oowekyala.jjtx.reporting.Severity
import com.github.oowekyala.jjtx.util.JjtxCoreEnvironment
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import junit.framework.TestCase
import java.nio.file.Path

/**
 * @author Clément Fournier
 */
abstract class JjtxTestBase : TestCase() {


    data class CtxTestBuilder(
        var grammarName: String = "Sample",
        var jccFile: String = JccElementFactory.DummyHeader,
        var opts: OptsSource = JsonOpts("{}"),
        var testGrammarDir: Path = java.nio.file.Files.createTempDirectory("jjtx-test"),
        var testOutputDir: Path = testGrammarDir.resolve("gen")
    ) {


        inner class TestEnv(private val myEnv: JjtxCoreEnvironment) : ParseUtilsMixin {

            override fun getProject(): Project = myEnv.project


            override fun String.asJccFile(): JccFile {

                val psiManager = PsiManager.getInstance(getProject())

                val virtualFile = LightVirtualFile("dummy.jjt", JjtreeFileType, this)

                return psiManager.findFile(virtualFile) as JccFile
            }

            val myCtx: JjtxContext by lazy {
                val optsFile =
                    if (opts is YamlOpts) "$grammarName.jjtopts.yaml"
                    else "$grammarName.jjtopts.json"

                val optsPath = testGrammarDir.resolve(optsFile)
                val f = optsPath.toFile()

                f.writeText(opts.str)

                JjtxContext.buildCtx(jccFile.asJccFile()) {
                    it.messageCollector = MessageCollector.create(it.io, false, Severity.FINE)
                    it.configChain = listOf(optsPath)
                }
            }


            init {
                myEnv.registerProjectComponent(GrammarOptionsService::class.java, JjtxFullOptionsService(myCtx))
            }

        }

        fun doTest(t: TestEnv.() -> Unit) {
            JjtxCoreEnvironment.withEnvironment {
                TestEnv(this).t()
            }
        }

    }

    fun testBuilder(base: CtxTestBuilder, ctxBuilder: CtxTestBuilder.() -> Unit) {

        CtxTestBuilder().copy(
            grammarName = base.grammarName,
            jccFile = base.jccFile,
            opts = base.opts,
            testGrammarDir = base.testGrammarDir,
            testOutputDir = base.testOutputDir
        ).let {
            it.ctxBuilder()
        }

    }

    fun testBuilder(ctxBuilder: CtxTestBuilder.() -> Unit): CtxTestBuilder =
        CtxTestBuilder().also { it.ctxBuilder() }

}


sealed class OptsSource {
    abstract val str: String
}

data class YamlOpts(override val str: String) : OptsSource()
data class JsonOpts(override val str: String) : OptsSource()

