package com.github.oowekyala.ijcc.lang.psi.stubs

import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.stubs.StubTreeLoader
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.2
 */
class JccStubTest : JccTestBase() {


    fun `test non-terminals are stubbed under the file`() = doTest(
        """
           $DummyHeader

            void foo() :{}{
                "foo"
            }

        """,
        """
            PsiFileStubImpl
              NON_TERMINAL:NonTerminalStubImpl
        """.trimIndent()
    )



    private fun doTest(@Language("JavaCC") code: String, expectedStubText: String) {
        val file = configureByText(code)
        val vFile = file.virtualFile!!
        runWriteAction {
            VfsUtil.saveText(vFile, code)
        }
        val stubTree = StubTreeLoader.getInstance().readFromVFile(project, vFile) ?: error("Stub tree is null")
        val stubText = DebugUtil.stubTreeToString(stubTree.root)
        LightPlatformCodeInsightFixtureTestCase.assertEquals(expectedStubText.trimIndent() + "\n", stubText)
    }
}
