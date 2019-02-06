package com.github.oowekyala.ijcc.insight.structureview

import com.github.oowekyala.ijcc.lang.psi.JccPsiElement
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.testFramework.PlatformTestUtil.assertTreeEqual
import com.intellij.testFramework.PlatformTestUtil.expandAll
import com.intellij.ui.RowIcon
import junit.framework.TestCase
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.1
 */
class StructureViewTest : JccTestBase() {


    private fun doPresentationDataTest(@Language("JavaCC") code: String, expectedPresentableText: String,
                                       isPublic: Boolean) {
        myFixture.configureByText("main.rs", code)
        val psi = myFixture.file.children.mapNotNull { it as? JccPsiElement }.first()
        val data = psi.getPresentationForStructure()
        TestCase.assertEquals(data.presentableText, expectedPresentableText)
        val icon = data.getIcon(false) as? RowIcon
        if (isPublic) {
            TestCase.assertNotNull(icon)
            TestCase.assertEquals(icon?.iconCount, 2);
        } else {
            if (icon != null) {
                TestCase.assertEquals(icon.iconCount, 1);
            }
        }
    }

    private fun doTest(@Language("JavaCC") code: String, expected: String) {
        val normExpected = expected.trimIndent() + "\n"
        myFixture.configureByText("dummy.jjt", code)
        myFixture.testStructureView {
            expandAll(it.tree)
            assertTreeEqual(it.tree, normExpected)
        }
    }
}