package com.github.oowekyala.jjtx

import com.github.oowekyala.ijcc.JjtreeFileType
import com.github.oowekyala.ijcc.lang.model.GrammarNature
import com.github.oowekyala.ijcc.lang.psi.JccFile
import com.github.oowekyala.ijcc.lang.psi.impl.GrammarOptionsService
import com.github.oowekyala.ijcc.lang.psi.impl.JccFileImpl
import com.github.oowekyala.ijcc.lang.util.ParseUtilsMixin
import com.github.oowekyala.jjtx.ide.JjtxOptionsService
import com.github.oowekyala.jjtx.util.JjtxCoreEnvironment
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import junit.framework.TestCase

/**
 * @author Clément Fournier
 */
abstract class JjtxTestBase : TestCase() {

    class TestEnv(private val myEnv: JjtxCoreEnvironment) : ParseUtilsMixin {

        override fun getProject(): Project = myEnv.project


        override fun String.asJccFile(): JccFile {

            val psiManager = PsiManager.getInstance(getProject())

            val virtualFile = LightVirtualFile("dummy.jjt", JjtreeFileType, this)

            val jcc = psiManager.findFile(virtualFile) as JccFile
            (jcc as JccFileImpl).grammarNature = GrammarNature.JJTRICKS
            return jcc
        }


        init {
            myEnv.registerProjectComponent(GrammarOptionsService::class.java, JjtxOptionsService())
        }

    }

    fun doTest(t: TestEnv.() -> Unit) {
        JjtxCoreEnvironment.withEnvironment {
            TestEnv(this).t()
        }
    }

}

