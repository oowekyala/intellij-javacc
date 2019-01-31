package com.github.oowekyala.ijcc.insight.quickdoc

import com.github.oowekyala.ijcc.insight.inspections.JavaccInspectionsProvider
import com.github.oowekyala.ijcc.lang.util.JccTestBase
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.psi.PsiElement
import com.intellij.testFramework.configureInspections
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
        configureByText(code)

        val (originalElement, _, offset) = findElementWithDataAndOffsetInEditor<PsiElement>()
        val element = DocumentationManager.getInstance(project)
            .findTargetElement(myFixture.editor, offset, myFixture.file, originalElement)!!

        val actual = JccDocumentationProvider.block(element, originalElement)?.trim()
            ?: error("Expected not null result")
        assertSameLines(expected.trimIndent(), actual)
    }

}