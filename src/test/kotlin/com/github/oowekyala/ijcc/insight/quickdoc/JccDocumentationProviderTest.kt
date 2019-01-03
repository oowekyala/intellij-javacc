package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.psi.PsiElement
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.0
 */
abstract class JccDocumentationProviderTest : JccTestBase() {


    protected fun doTest(
        @Language("JavaCC") code: String,
        @Language("Html") expected: String,
        block: JccDocumentationProvider.(PsiElement, PsiElement?) -> String?
    ) {
        InlineFile(code)

        val (originalElement, _, offset) = findElementWithDataAndOffsetInEditor<PsiElement>()
        val element = DocumentationManager.getInstance(project)
            .findTargetElement(myFixture.editor, offset, myFixture.file, originalElement)!!

        val actual = JccDocumentationProvider.block(element, originalElement)?.trim()
            ?: error("Expected not null result")
        assertSameLines(expected.trimIndent(), actual)
    }


    protected fun doUrlTestByText(@Language("JavaCC") text: String, expectedUrl: String?) =
            doUrlTest(text, expectedUrl, this::configureByText)

    private fun doUrlTest(
        @Language("JavaCC") text: String,
        expectedUrl: String?,
        configure: (String) -> Unit
    ) {
        configure(text)

        val (originalElement, _, offset) = findElementWithDataAndOffsetInEditor<PsiElement>()
        val element = DocumentationManager.getInstance(project)
            .findTargetElement(myFixture.editor, offset, myFixture.file, originalElement)!!

        val action: () -> Unit = {
            val actualUrls = JccDocumentationProvider.getUrlFor(element, originalElement)
            assertEquals(listOfNotNull(expectedUrl), actualUrls)
        }
        action()
    }

}