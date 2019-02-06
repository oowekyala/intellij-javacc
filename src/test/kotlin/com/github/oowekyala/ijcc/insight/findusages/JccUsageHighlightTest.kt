package com.github.oowekyala.ijcc.insight.findusages

import com.github.oowekyala.ijcc.util.JccAnnotationTestBase
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.highlighting.actions.HighlightUsagesAction
import com.intellij.testFramework.ExpectedHighlightingData
import org.intellij.lang.annotations.Language

/**
 * @author Clément Fournier
 * @since 1.1
 */
class JccUsageHighlightTest : JccAnnotationTestBase() {


    fun `test production usages from decl`() = doTest(
        """
            $DummyHeader

            void ${h("Fo~o")}():
            {}
            {
                bar() "foo" "zlatan" ${h("Foo")}()
            }

            void bar(): {}
            {
                "f" ${h("Foo")}()
            }

         """
    )

    fun `test production usages from ref`() = doTest(
        """
            $DummyHeader

            void ${h("Foo")}():
            {}
            {
                bar() "foo" "zlatan" ${h("Fo~o")}()
            }

            void bar(): {}
            {
                "f" ${h("Foo")}()
            }

         """
    )

    fun `test token usages from ref`() = doTest(
        """
            $DummyHeader

            TOKEN: {
              < {h("Foo")} : "foo" >
            | <BAR : ("bar") >
            }

            void Foo():
            {}
            {
                "hey" ( "i" | <${h("Fo~o")}> )
            }
         """
    )
    fun `test token usages from decl`() = doTest(
        """
            $DummyHeader

            TOKEN: {
              < ${h("Fo~o")} : "foo" >
            | <BAR : ("bar") >
            }

            void Foo():
            {}
            {
                "hey" ( "i" | <${h("Foo")}> )
            }
         """
    )

    private fun h(content: String) = infoAnnot(content, "null")


    private fun doTest(@Language("JavaCC") code: String) {
        configureByText(code)
        val document = myFixture.editor.document
        val data = ExpectedHighlightingData(document, false, false, true, false, myFixture.file)
        data.init()

        val caret = document.extractMarkerOffset(project, CARET_TAG)
        assert(caret != -1) { "Caret marker '$CARET_TAG' expected" }
        editor.caretModel.moveToOffset(caret)

        myFixture.testAction(HighlightUsagesAction())
        val highlighters = myFixture.editor.markupModel.allHighlighters

        val infos = highlighters.map { highlighter ->
            var startOffset = highlighter.startOffset
            var endOffset = highlighter.endOffset

            if (startOffset > caret) startOffset += CARET_TAG.length
            if (endOffset > caret) endOffset += CARET_TAG.length

            HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(startOffset, endOffset).create()
        }

        data.checkResult(infos, StringBuilder(document.text).toString())
    }

    private companion object {
        const val CARET_TAG = "~"
    }
}